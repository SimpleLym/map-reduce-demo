package com.lee.mr.remoting.transport.netty4;

import com.alibaba.fastjson.JSONObject;
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
        this.channelHandler.channelActive(ctx);
    }

  /*  @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("client write"+msg);
        //super.write(ctx,msg,promise);
        channelHandler.send(ctx,(String)msg);
    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("client read："+msg.toString());
        JSONObject json = new JSONObject();
        json.put("code", 0);
        json.put("type", "map");
        json.put("filePath", "D:\\workspace\\6.824\\map\\out");
        this.channelHandler.received(ctx,msg.toString());
        /*String s = json.toJSONString() + Constant.MSG_TAIL;
        System.out.println(s);
        ctx.writeAndFlush(s);*/
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

    }
}
