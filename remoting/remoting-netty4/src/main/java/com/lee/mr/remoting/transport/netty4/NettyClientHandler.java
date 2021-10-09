package com.lee.mr.remoting.transport.netty4;

import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class NettyClientHandler extends ChannelDuplexHandler {
    ChannelHandler channelHandler;
    public NettyClientHandler(ChannelHandler handler) {
        this.channelHandler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client active");
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("client write"+msg);
        super.write(ctx,msg,promise);
        channelHandler.send(ctx,(String)msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("client read");
        this.channelHandler.received(ctx,msg.toString());
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

    }
}
