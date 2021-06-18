/**
 * @author ：lym
 * @date ：Created in 2021/6/18 15:22
 */
package com.lee.mr.remoting.transport.api;

public interface Client {
    boolean connect(String ip,int port);
    void send();
    void receive();
    void close();
}
