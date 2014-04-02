package com.cagricelebi.datagw.ejb;

import com.cagricelebi.datagw.lib.log.Logger;
import com.cagricelebi.datagw.lib.model.Transaction;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(
        name = "DgwServerTransactionProcessorEJB",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/datagw/server/processing"),
            @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "5")
        }
)
public class DgwServerTransactionProcessorBean implements MessageListener {

    @Inject
    Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            String body = ((TextMessage) message).getText();
            logger.log("(onMessage) Processing Request: " + body);
            Transaction tx = Transaction.fromJson(body);
            process(tx);
        } catch (Exception e) {
            logger.log(e);
        }
    }

    private void process(Transaction tx) {
        try {
            logger.log("(process) Transaction status updated to STATUS_PROCESSING.");
            tx.setStatus(Transaction.Status.STATUS_PROCESSING);
            logger.log("(process) Reading data.........");
            String data = tx.getString();
            // now you can do fancy stuff with the data.
            // ...
            logger.log("(process) Transaction status updated to STATUS_COMPLETE.");
            tx.setStatus(Transaction.Status.STATUS_COMPLETE);
            // message consumed.
            logger.log("(process) Consumed successfully.");
        } catch (Exception e) {
            tx.setStatus(Transaction.Status.STATUS_ERROR);
            // TODO generate alert email, notification etc?
            logger.log(e);
        }
    }

}
