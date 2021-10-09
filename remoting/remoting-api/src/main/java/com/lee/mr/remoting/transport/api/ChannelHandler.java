package com.lee.mr.remoting.transport.api;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface ChannelHandler {
    void received(ChannelHandlerContext ctx,String msg);
    void send(ChannelHandlerContext ctx, String msg);
    void register(ChannelHandlerContext ctx);
}
