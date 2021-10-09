package com.lee.mr.remoting.transport.api;

public interface Server {
    void openServer();
    void close();
    void send();
}
