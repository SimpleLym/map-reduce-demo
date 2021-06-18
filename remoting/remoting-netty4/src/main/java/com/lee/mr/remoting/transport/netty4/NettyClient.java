/**
 * @author ：lym
 * @date ：Created in 2021/6/18 15:55
 */
package com.lee.mr.remoting.transport.netty4;

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

import java.util.concurrent.TimeUnit;

public class NettyClient implements Client {
    Bootstrap bootstrap;
    int connectTimeout;
    String host;
    int port;
    Channel channel;
    public NettyClient() {
        host = "127.0.0.1";
        port = 8085;
        connectTimeout = 8000;
    }

    @Override
    public boolean connect(String host,int port) {
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

                        }
                    });
            ChannelFuture f = bootstrap.connect(host, port);
            boolean ret = f.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
            if(ret&&f.isSuccess()){
                Channel newChannel = f.channel();
                Channel oldChannel = NettyClient.this.channel;
                if(oldChannel!=null){
                    oldChannel.close();
                }
                System.out.println("1:"+ this.channel.localAddress());
            }


            //channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
        }
        return false;
    }

    @Override
    public void send() {

    }

    @Override
    public void receive() {

    }

    @Override
    public void close() {

    }
}
