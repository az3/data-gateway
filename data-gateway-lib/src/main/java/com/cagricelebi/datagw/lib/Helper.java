package com.cagricelebi.datagw.lib;

import com.cagricelebi.datagw.lib.log.Logger;
import com.cagricelebi.datagw.lib.model.Transaction;
import com.cagricelebi.datagw.lib.queue.JmsUtility;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

public class Helper {

    /**
     * Beware, the valid word "null" is also rendered as empty.
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || "".contentEquals(str) || "null".contentEquals(str);
    }

    public static boolean isEmpty(Map<String, String> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Transforms dotted IP address to long number.
     *
     * @param ipStr Dotted ip ie. "127.0.0.1"
     * @return Long number ie. "2130706433"
     */
    public static long ip2Long(String ipStr) {
        long result = 0;
        try {
            InetAddress ip = InetAddress.getByName(ipStr);
            byte[] octets = ip.getAddress();
            for (byte octet : octets) {
                result <<= 8;
                result |= octet & 0xff;
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Transforms long number to dotted IP address.
     * Does not check validity.
     *
     * @param l Long number ie. "2130706433"
     * @return Dotted ip ie. "127.0.0.1"
     */
    public static String long2Ip(long l) {
        return ((l >> 24) & 0xFF) + "."
                + ((l >> 16) & 0xFF) + "."
                + ((l >> 8) & 0xFF) + "."
                + (l & 0xFF);
    }

    /**
     * Checks validity of a dotted IP address string.
     * Uses {@link java.net.InetAddress}.getByName() method.
     *
     * @param ipStr
     * @return
     */
    public static boolean isValidIp(String ipStr) {
        try {
            InetAddress ip = InetAddress.getByName(ipStr);
            // logger.log("A valid IP (" + ipStr + ") queried.");
            return ip2Long(ipStr) > 0;
        } catch (Exception e) {
            // logger.log("Queried IP (" + ipStr + ") is not valid.");
            return false;
        }
    }

    /**
     * Checks tryCount of a transaction and tries to send it to the queue specified, unless retry limit reached.
     * I am not sure about the grammar of the previous sentence.
     * And the usage of injected logger is not nice.
     *
     * @param queueName
     * @param tx
     * @param logger
     */
    public static void retry(String queueName, Transaction tx, Logger logger) {
        if (tx.getTryCount() > 0) {
            long duration = Statics.RETRY_DURATIONS.get(tx.getTryCount());
            tx.setTryCount(tx.getTryCount() - 1);
            logger.log("(retry) Transaction tryCount decreased to:" + tx.getTryCount());
            logger.log("(retry) Message sent to queue: " + queueName);
            JmsUtility.scheduleTextMessage(queueName, tx.toJson(), duration);
        } else {
            logger.log("(retry) Rescheduling to send cancelled. retryCount is " + tx.getTryCount());
        }
    }

    /**
     * Read header values and log them in filesystem.
     *
     * @param request
     * @param logger Injected logger.
     */
    public static void logRequestDetails(HttpServletRequest request, Logger logger) {
        try {
            logger.log("(logRequestDetails) Read request headers of incoming uri query: " + request.getQueryString());
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String headerName = headers.nextElement();
                String headerValue = request.getHeader(headerName);
                logger.log("(logRequestDetails) " + headerName + " : " + headerValue);
            }
        } catch (Exception e) {
            logger.log(e);
        }
    }

    /**
     * Uses X-Forwarded-For or request.getRemoteAddr() as fallback.
     *
     * @param request
     * @return Dotted IP address in format "xxx.xxx.xxx.xxx".
     */
    public static String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress;
        if (request.getHeader("X-Forwarded-For") != null) {
            remoteAddress = request.getHeader("X-Forwarded-For");
            if (Helper.isValidIp(remoteAddress)) {
                return remoteAddress;
            }
        }
        if (request.getHeader("x-forwarded-for") != null) {
            remoteAddress = request.getHeader("x-forwarded-for");
            if (Helper.isValidIp(remoteAddress)) {
                return remoteAddress;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Returns a valid IP v4 address.
     *
     * @return [0-255].[0-255].[0-255].[0-255]
     */
    public static String generateRandomIp() {
        try {
            Random r = new Random();
            String ip = r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
            if (isValidIp(ip)) {
                return ip;
            } else {
                return generateRandomIp();
            }
        } catch (Exception e) {
        }
        return "127.0.0.1";
    }

    /**
     * For logging.
     *
     * @param map
     * @return
     */
    public static String map2str(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        try {
            if (!isEmpty(map)) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();
                    sb.append(key).append(':').append(val).append(',');
                }
                return sb.substring(0, sb.length() - 1);
            }
        } catch (Exception e) {
        }
        return sb.toString();
    }

    /**
     * For logging.
     *
     * @param arr
     * @return
     */
    public static String array2str(String[] arr) {
        String output = "[";
        try {
            for (int i = 0; i < arr.length; i++) {
                String string = arr[i];
                output += string + ", ";
            }
            output = output.substring(0, output.length() - 2);
        } catch (Exception e) {
        }
        return output + "]";
    }

    /**
     * Simple matcher that finds given regular expression patterns.
     * Samples:<br />
     * contains("selam", new String[]{"q", "r"}) -> returns false.<br />
     * contains("selam", new String[]{"q", "a"}) -> returns true.<br />
     * contains("selam", new String[]{"q", "a$"}) -> returns false.<br />
     * contains("selam", new String[]{"q", "m$"}) -> returns true.<br />
     * contains("selam", new String[]{"^m"}) -> returns false.<br />
     *
     * @param str Source string to search for.
     * @param regularExpressions Regular Expressions as an array.
     * @return TRUE if ANY of the given patterns match. FALSE, if ALL of the given patterns are not found.
     */
    public static boolean contains(String str, String[] regularExpressions) {
        for (String regularExpression : regularExpressions) {
            Pattern pattern = Pattern.compile(regularExpression);
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

}
