package com.cagricelebi.datagw.lib.model;

import com.google.gson.Gson;
import java.util.HashMap;

public class Transaction {

    private long id;
    private String string;
    private long ip;
    private int status;
    private long timestamp;
    private int tryCount = 7;
    private HashMap<String, String> parameters;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Transaction{" + "id=" + id + ", string=" + string + ", ip=" + ip + ", status=" + status + ", timestamp=" + timestamp + ", tryCount=" + tryCount + "}";
    }

    public static Transaction fromJson(String body) throws Exception {
        return new Gson().fromJson(body, Transaction.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static class Status {

        public static final int STATUS_ERROR = -1;
        public static final int STATUS_SPOOLED = 0;
        public static final int STATUS_CREATED = 1;
        public static final int STATUS_VERIFIED = 2;
        public static final int STATUS_FLOOD = -3;
        public static final int STATUS_PROCESSING = 4;
        public static final int STATUS_COMPLETE = 5;

    }

}
