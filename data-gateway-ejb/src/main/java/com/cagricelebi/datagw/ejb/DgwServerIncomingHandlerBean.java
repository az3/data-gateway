package com.cagricelebi.datagw.ejb;

import com.cagricelebi.datagw.lib.Helper;
import com.cagricelebi.datagw.lib.Statics;
import com.cagricelebi.datagw.lib.log.Logger;
import com.cagricelebi.datagw.lib.model.Transaction;
import com.cagricelebi.datagw.lib.queue.JmsUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.infinispan.Cache;

@MessageDriven(
        name = "DgwServerIncomingHandlerEJB",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/datagw/server/incoming")
        }
)
public class DgwServerIncomingHandlerBean implements MessageListener {

    @Inject
    Logger logger;

    @Resource(lookup = "java:/infinispan/DGW-CACHE/incomingIpCache")
    private Cache<Long, List<Long>> incomingIpCache;

    @Override
    public void onMessage(Message message) {
        try {
            String body = ((TextMessage) message).getText();
            logger.log("(onMessage) Incoming Request: " + body);
            Transaction tx = Transaction.fromJson(body);
            handle(tx);
        } catch (Exception e) {
            logger.log(e);
        }
    }

    private void handle(Transaction tx) {
        try {
            logger.log("(handle) Analysing transaction for flood/spam.");
            // Security check: 1 ip can send 10 requests within 1 seconds.
            // This control must be fast, and the data processing procedure can take long.
            // Thus, the data processing queue is seperated. 
            // But they can be merged with the proper algorithm.

            long longIp = tx.getIp();
            logger.log("(handle) Reading IP address of request: " + longIp + " -> " + Helper.long2Ip(longIp) + ".");
            long oneSecondAgo = System.currentTimeMillis() - 1000;
            List<Long> timestamps = incomingIpCache.get(longIp);
            if (timestamps == null || timestamps.isEmpty()) {
                timestamps = new ArrayList<>();
            }
            logger.log("(handle) Number of recent requests from the same IP: " + timestamps.size() + ".");

            if (timestamps.size() > 9) {
                logger.log("(handle) Too many recent requests in the last 1 second, analyse them.");
                List<Long> smallerList = new ArrayList<>();
                for (long timestamp : timestamps) {
                    if (timestamp > oneSecondAgo) {
                        smallerList.add(timestamp);
                    }
                }
                if (smallerList.size() > 9) {
                    logger.log("(handle) Flood detected for ip: " + Helper.long2Ip(longIp) + " with " + smallerList.size() + " operations.");
                    logger.log("(handle) Transaction status updated to STATUS_FLOOD.");
                    tx.setStatus(Transaction.Status.STATUS_FLOOD);
                    // TODO This can be handled in another queue. Maybe create security alert for that specific IP, player?
                } else {
                    logger.log("(handle) In the last 1 second, only " + smallerList.size() + " requests came, continue processing.");
                    smallerList.add(tx.getTimestamp());
                    incomingIpCache.put(longIp, smallerList, 1, TimeUnit.SECONDS);
                    sendForProcessing(tx);
                }
            } else {
                logger.log("(handle) In the last 1 second, only " + timestamps.size() + " requests came, continue processing.");
                timestamps.add(tx.getTimestamp());
                incomingIpCache.put(longIp, timestamps, 1, TimeUnit.SECONDS);
                sendForProcessing(tx);
            }

        } catch (Exception e) {
            logger.log(e);
        }
    }

    private void sendForProcessing(Transaction tx) {
        logger.log("(sendForProcessing) Transaction status updated to STATUS_VERIFIED.");
        tx.setStatus(Transaction.Status.STATUS_VERIFIED);
        logger.log("(sendForProcessing) Message sent to queue: " + Statics.QUEUE_PROCESSING);
        JmsUtility.queueTextMessage(Statics.QUEUE_PROCESSING, tx.getString());
    }

}
