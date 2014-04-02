package com.cagricelebi.datagw.ejb;

import com.cagricelebi.datagw.lib.Helper;
import com.cagricelebi.datagw.lib.Statics;
import com.cagricelebi.datagw.lib.connection.HttpConnectionHandler;
import com.cagricelebi.datagw.lib.connection.Response;
import com.cagricelebi.datagw.lib.log.Logger;
import com.cagricelebi.datagw.lib.model.Transaction;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * This MDB acts as a local buffer on the client system.
 * It sends transactions to the API on the server.
 *
 * @author cagri.celebi
 */
@MessageDriven(
        name = "DgwClientSpoolerEJB",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/datagw/client/spooler")
        }
)
public class DgwClientSpoolerBean implements MessageListener {

    @Inject
    Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            String body = ((TextMessage) message).getText();
            logger.log("(onMessage) Spooled Request: " + body);
            Transaction tx = Transaction.fromJson(body);
            handle(tx);
        } catch (Exception e) {
            logger.log(e);
        }
    }

    private void handle(Transaction tx) {
        try {
            logger.log("(handle) Trying to send the data to main server.");
            Response r = HttpConnectionHandler.postJson(Statics.ENDPOINT_SERVER, tx.toJson());
            if (r == null || r.getHttpResponseCode() != 200) {
                logger.log("(handle) Connection error, retry.");
                Helper.retry(Statics.QUEUE_SPOOLER, tx, logger);
            } else {
                logger.log("(handle) Transaction sent to server successfully.");
            }
        } catch (Exception e) {
            logger.log(e);
        }
    }

}
