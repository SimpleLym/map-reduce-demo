package com.lee.mr.remoting.transport.netty4;

import com.lee.mr.remoting.transport.api.ChannelHandler;
import com.lee.mr.remoting.transport.api.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.SocketAddress;

public class NettyServer implements Server {
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    SocketAddress socketAddress;
    ChannelHandler channelHandler;
    public NettyServer(SocketAddress socketAddress,ChannelHandler channelHandler){
        this.socketAddress = socketAddress;
        this.channelHandler = channelHandler;
    }
    @Override
    public void openServer() {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR,true)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast(new NettyServerHandler(channelHandler));
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(socketAddress);
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    @Override
    public void close() {

    }

    @Override
    public void send() {

    }

}
