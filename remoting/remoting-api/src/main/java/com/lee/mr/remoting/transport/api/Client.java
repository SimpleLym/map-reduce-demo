/**
 * @author ：lym
 * @date ：Created in 2021/6/18 15:22
 */
package com.lee.mr.remoting.transport.api;

import com.lee.mr.common.constant.ClientStatus;
import com.lee.mr.common.constant.URL;

public interface Client {
    boolean connect();
    void send(Object message);
    void close();
    boolean isAlive();
    void heartBeat(URL url);
    String getStatus();
}
