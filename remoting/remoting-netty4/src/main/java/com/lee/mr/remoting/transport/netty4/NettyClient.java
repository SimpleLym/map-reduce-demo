/**
 * @author ：lym
 * @date ：Created in 2021/6/18 15:55
 */
package com.lee.mr.remoting.transport.netty4;

import com.lee.mr.common.constant.URL;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import com.lee.mr.remoting.transport.api.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.lee.mr.common.constant.Constant.MSG_TAIL;

public class NettyClient implements Client {
    Bootstrap bootstrap;
    int connectTimeout;
    Channel channel;
    URL url;
    boolean closed;
    ChannelHandler channelHandler;
    volatile String status;
    URL clientUrl;
    public NettyClient(URL url,URL clientUrl,ChannelHandler channelHandler) {
        this.url = url;
        this.clientUrl = clientUrl;
        connectTimeout = 8000;
        this.channelHandler = channelHandler;
    }

    @Override
    public boolean connect() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast(new NettyClientHandler(channelHandler));
                        }
                    });
            ChannelFuture f = bootstrap.connect(new InetSocketAddress(url.getIp(), url.getPort()),new InetSocketAddress(clientUrl.getIp(),clientUrl.getPort()));
            boolean ret = f.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
            if (ret && f.isSuccess()) {
                Channel newChannel = f.channel();
                try {
                    Channel oldChannel = NettyClient.this.channel;
                    if (oldChannel != null) {
                        oldChannel.close();
                        System.out.println("1:" + this.channel.localAddress());
                    }
                }finally {
                    if(NettyClient.this.isClosed()){
                        try {
                            newChannel.close();
                        }finally {
                            NettyClient.this.channel = null;
                        }
                    } else {
                        NettyClient.this.channel = newChannel;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //workerGroup.shutdownGracefully();
        }
        return channel==null?false:channel.isActive();
    }

    @Override
    public void send(Object message) {
        if(this.isClosed()){
            throw new RuntimeException("this client is closed!");
        }
        ChannelFuture channelFuture = channel.writeAndFlush(message+MSG_TAIL);
        try {
            channelFuture.await(connectTimeout);
            Throwable cause = channelFuture.cause();
            if(cause!=null){
                throw cause;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isAlive() {
        return this.channel.isActive();
    }

    @Override
    public void heartBeat(URL url) {

    }

    @Override
    public String getStatus() {
        return null;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
