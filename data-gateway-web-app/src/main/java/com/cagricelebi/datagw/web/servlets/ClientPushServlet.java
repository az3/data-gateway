package com.cagricelebi.datagw.web.servlets;

import com.cagricelebi.datagw.lib.Helper;
import com.cagricelebi.datagw.lib.Statics;
import com.cagricelebi.datagw.lib.log.Logger;
import com.cagricelebi.datagw.lib.model.Transaction;
import com.cagricelebi.datagw.lib.queue.JmsUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the entry point for clients.
 *
 * @author cagri.celebi
 */
@WebServlet(name = "ClientPushServlet", urlPatterns = {"/client"})
public class ClientPushServlet extends HttpServlet {

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
                tx.setIp(Helper.ip2Long(Helper.generateRandomIp())); // TODO This is TEST.
                tx.setStatus(Transaction.Status.STATUS_SPOOLED);
                tx.setString(body.trim()); // sometimes endline is added.
                tx.setTimestamp(System.currentTimeMillis());
                logger.log("(processRequest) Transaction constructed: " + tx.toJson());
                // Queue this client data for sending it to main server by DgwClientSpoolerBean.
                logger.log("(processRequest) Message sent to queue: " + Statics.QUEUE_SPOOLER);
                JmsUtility.queueTextMessage(Statics.QUEUE_SPOOLER, tx.toJson());
            }
        } catch (Exception e) {
            tx.setStatus(Transaction.Status.STATUS_ERROR);
            logger.log(e);
        } finally {
            try (PrintWriter out = response.getWriter()) {
                out.print(tx.toJson());
                logger.log("(processRequest) Client API output: " + tx.toJson());
            } catch (Exception e) {
                logger.log(e);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO maybe method not supported error and/or explanation on how to use?
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
