package com.cagricelebi.datagw.lib.connection;

public class Response {

    private boolean error;
    private int httpResponseCode; // -1, 200, 201, 403, 404, 500
    private String httpResponseMessage; // Exception, OK, Created, Forbidden, Not Found, Internal Server Error
    private String output; // returned value.

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
        try {
            String responseCode = "" + httpResponseCode;
            if (responseCode.startsWith("2")) {
                error = false;
            } else {
                error = true;
            }
        } catch (Exception e) {
        }
    }

    public String getHttpResponseMessage() {
        return httpResponseMessage;
    }

    public void setHttpResponseMessage(String httpResponseMessage) {
        this.httpResponseMessage = httpResponseMessage;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "Response{" + "error=" + error + ", httpResponseCode=" + httpResponseCode + ", httpResponseMessage=" + httpResponseMessage + ", output=" + output + "}";
    }

}
