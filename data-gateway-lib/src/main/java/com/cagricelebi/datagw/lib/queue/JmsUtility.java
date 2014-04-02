package com.cagricelebi.datagw.lib.queue;

import com.cagricelebi.datagw.lib.log.Logger;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class JmsUtility {

    private static final Logger logger = Logger.getLogger(JmsUtility.class.getName());

    /**
     * Send message to queue.
     *
     * @param queueName Queue name. must exist in configuration file, preferably durable.
     * @param jsonMessage Text data to push. usually JSON.
     * @return
     */
    public static boolean queueTextMessage(String queueName, String jsonMessage) {
        return queue(queueName, jsonMessage, 0);
    }

    /**
     * Send delayed message.
     *
     * @param queueName queue name. must exist in configuration file, preferably durable.
     * @param jsonMessage Text data to push. usually JSON.
     * @param delay time to delay message in milliseconds. For 1 minute = input 60000.
     * @return
     */
    public static boolean scheduleTextMessage(String queueName, String jsonMessage, long delay) {
        return queue(queueName, jsonMessage, delay);
    }

    private static boolean queue(String queueName, String textMessage, long delay) {
        try {
            InitialContext initialContext = new InitialContext();
            if (queueName != null && !queueName.equals("")) {
                QueueConnectionFactory connectionFactory = (QueueConnectionFactory) initialContext.lookup("java:/JmsXA");
                Queue queue = (Queue) initialContext.lookup(queueName);
                return queue(connectionFactory, queue, textMessage, delay);
            } else {
                logger.log("Error: Empty queue name for msg: " + textMessage);
                return false;
            }
        } catch (Exception e) {
            logger.log("Error: Exception in PooledConnection, will try ConnectionFactory for queue: " + queueName + ", msg: " + textMessage);
            logger.log(e);
            try {
                InitialContext initialContext = new InitialContext();
                QueueConnectionFactory connectionFactory = (QueueConnectionFactory) initialContext.lookup("java:/ConnectionFactory");
                Queue queue = (Queue) initialContext.lookup(queueName);
                return queue(connectionFactory, queue, textMessage, delay);
            } catch (Exception ex) {
                logger.log("Error: Exception in ConnectionFactory for queue: " + queueName + ", msg: " + textMessage);
                logger.log(e);
            }
        }
        return false;
    }

    private static boolean queue(QueueConnectionFactory connectionFactory, Queue queue, String textMessage, long delay) {
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        QueueSender queueSender = null;
        try {
            queueConnection = connectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            queueSender = queueSession.createSender(queue);
            queueConnection.start();

            TextMessage message = queueSession.createTextMessage(textMessage);
            if (delay > 0) {
                message.setLongProperty("_HQ_SCHED_DELIVERY", System.currentTimeMillis() + delay); // HornetQ only.
            }
            queueSender.send(message);
            return true;
        } catch (Exception e) {
            logger.log(e);
        } finally {
            if (queueSender != null) {
                try {
                    queueSender.close();
                } catch (Exception e) {
                    logger.log(e);
                }
            }
            if (queueSession != null) {
                try {
                    queueSession.close();
                } catch (Exception e) {
                    logger.log(e);
                }
            }
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (Exception e) {
                    logger.log(e);
                }
            }
        }
        return false;
    }

}
