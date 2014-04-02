package com.cagricelebi.datagw.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Statics {

    /**
     * 1 min, 3 min, 3 min, 1 hour, 1 hour, 2 hours, 2 hours = 7 retries
     */
    public static final Map<Integer, Long> RETRY_DURATIONS;

    static {
        Map<Integer, Long> retryMap = new HashMap<>();
        retryMap.put(7, (long) 60000);
        retryMap.put(6, (long) 180000);
        retryMap.put(5, (long) 180000);
        retryMap.put(4, (long) 3600000);
        retryMap.put(3, (long) 3600000);
        retryMap.put(2, (long) 7200000);
        retryMap.put(1, (long) 7200000);
        RETRY_DURATIONS = Collections.unmodifiableMap(retryMap);
    }

    /**
     * This queue is used in client side.
     * Local spooler handles all incoming requests from clients and sends the data to server-api.
     */
    public static final String QUEUE_SPOOLER = "queue/datagw/client/spooler";

    /**
     * Location of the server-api.
     * Client data will be sent to here.
     */
    public static final String ENDPOINT_SERVER = "http://localhost:8080/data-gateway-web-app/server";

    /**
     * This queue is used in server side.
     * The first layer is here, it checks IP constraint.
     */
    public static final String QUEUE_INCOMING = "queue/datagw/server/incoming";

    /**
     * This queue is used in server-side.
     * The second layer is here, it inserts the data to (r)dbms or filesystem.
     */
    public static final String QUEUE_PROCESSING = "queue/datagw/server/processing";

}
