package com.lee.mr.master.cluster;

import com.lee.mr.common.constant.URL;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ClientProxy {
    Channel channel;
    ChannelHandlerContext ctx;
    URL url;
    String status;
    ChannelHandler handler;

    public ClientProxy(ChannelHandlerContext ctx, ChannelHandler handler) {
        this.ctx = ctx;
        this.handler = handler;
        this.channel = ctx.channel();
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAlive(){
        return channel.isActive();
    }

    public void send(Object msg){
        this.handler.send(this.ctx,(String)msg);
    }
}
