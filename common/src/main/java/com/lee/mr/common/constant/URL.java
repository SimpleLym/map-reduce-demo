package com.lee.mr.common.constant;

import java.util.Map;
import java.util.Objects;

public class URL {
    String ip;
    int port;
    private Map<String, String> parameters;

    public URL(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public URL(String address) {
        if(address.indexOf(":")>-1){
            this.ip = address.split(":")[0];
            this.port = Integer.parseInt(address.split(":")[1]);
        }
    }

    public URL(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URL url = (URL) o;
        return port == url.port &&
                Objects.equals(ip, url.ip) &&
                Objects.equals(parameters, url.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, parameters);
    }

    @Override
    public String toString() {
        String url = this.ip+":"+port+"?";
        if(parameters!=null) {
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                url += e.getKey() + "=" + e.getValue() + "&";
            }
        }
        url = url.substring(0,url.length()-1);
        return url;
    }
}
