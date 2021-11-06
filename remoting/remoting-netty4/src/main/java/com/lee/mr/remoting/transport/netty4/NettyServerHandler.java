package com.lee.mr.remoting.transport.netty4;

import com.lee.mr.common.constant.Task;
import com.lee.mr.common.split.DefaultDataSplit;
import com.lee.mr.common.split.IDataSplit;
import com.lee.mr.common.split.InputSplit;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class NettyServerHandler extends ChannelDuplexHandler {
    ChannelHandler channelHandler;

    public NettyServerHandler(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

/*    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx,msg,promise);
        channelHandler.send(ctx,msg.toString());
    }*/

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channelHandler.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        System.out.println("received "+msg);
        channelHandler.received(ctx,msg.toString());
        //ctx.writeAndFlush(msg.toString()+"\r\n");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        channelHandler.register(ctx);
    }

}
