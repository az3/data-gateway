package com.cagricelebi.datagw.web.servlets;

import com.cagricelebi.datagw.lib.Helper;
import com.cagricelebi.datagw.lib.Statics;
import com.cagricelebi.datagw.lib.log.Logger;
import com.cagricelebi.datagw.lib.model.Transaction;
import com.cagricelebi.datagw.lib.queue.JmsUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the entry point on server.
 *
 * @author cagri.celebi
 */
@WebServlet(name = "ServerPushServlet", urlPatterns = {"/server"})
public class ServerPushServlet extends HttpServlet {

    @Inject
    Logger logger;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Helper.logRequestDetails(request, logger);
        Transaction tx = new Transaction();
        try {
            
            logger.log("(processRequest) Reading POSTed data...");
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            String body = sb.toString();
            logger.log("(processRequest) Payload: " + body);
            
            if (!Helper.isEmpty(body)) {
                String decoded = URLDecoder.decode(body.trim(), "UTF-8");
                tx = Transaction.fromJson(decoded);
                logger.log("(processRequest) Transaction re-constructed: " + tx.toJson());
                // if body is a legit Transaction object, continue.
                logger.log("(processRequest) Message sent to queue: " + Statics.QUEUE_INCOMING);
                JmsUtility.queueTextMessage(Statics.QUEUE_INCOMING, decoded);
            }
        } catch (Exception e) {
            tx.setStatus(Transaction.Status.STATUS_ERROR);
            logger.log(e);
        } finally {
            try (PrintWriter out = response.getWriter()) {
                out.print(tx.toJson());
                logger.log("(processRequest) Server API output: " + tx.toJson());
            } catch (Exception e) {
                logger.log(e);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO maybe method not supported error?
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

}
