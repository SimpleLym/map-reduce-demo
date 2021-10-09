package com.lee.mr.master.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.ClientStatus;
import com.lee.mr.common.constant.Task;
import com.lee.mr.common.constant.URL;
import com.lee.mr.master.cluster.Cluster;
import com.lee.mr.master.cluster.MRCluster;
import com.lee.mr.master.cluster.ClientProxy;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.lee.mr.common.constant.Constant.MSG_TAIL;

public class MasterChannelHandler implements ChannelHandler {
    String[] workersUrl;
    //Map<String,Channel> map;
    MRCluster cluster;
    Consumer<Cluster> consumer;
    AtomicInteger counter;

    public MasterChannelHandler(String[] workersUrl, MRCluster cluster, Consumer<Cluster> consumer) {
        this.workersUrl = workersUrl;
        //this.map = new ConcurrentHashMap<>();
        this.cluster = cluster;
        this.consumer = consumer;
    }

    @Override
    public void received(ChannelHandlerContext ctx, String msg) {
        System.out.println("receive:"+msg);
        JSONObject json = JSONObject.parseObject(msg);
        if(json!=null) {
            String type = json.getString("type");
            int code = json.getInteger("code");
            if (code == 0 && "map".equals(type)) {
                JSONArray filePaths = json.getJSONArray("filePath");
                Queue queue = new LinkedBlockingQueue();
                for (int i = 0; i < filePaths.size(); i++) {
                    Task task = new Task("reduce", filePaths.getString(i));
                    queue.add(task);
                }
                cluster.dispatch(queue);
            } else if (code == 0 && "reduce".equals(type)) {
                if (counter.getAndIncrement() == workersUrl.length) {
                    //输出结果
                }
            } else if ("init".equals(type)) {
                Channel channel = ctx.channel();
                System.out.println(channel.remoteAddress() + " connected!");
                for (String url : workersUrl) {
                    String ip = url.split(":")[0];
                    int port = Integer.parseInt(url.split(":")[1]);
                    URL url1 = new URL(ip, port);
                    if (channel.remoteAddress()
                            .equals(new InetSocketAddress(ip, port))) {
                        cluster.remove(url1);
                        //NettyClient nettyClient = new NettyClient(url1);
                        ClientProxy client = new ClientProxy(ctx, this);
                        client.setUrl(url1);
                        client.setStatus(ClientStatus.CONNECTED);
                        cluster.join(url1, client);
                /*boolean isConnect = nettyClient.connect();
                if(isConnect) {
                    cluster.join(url1, nettyClient);
                }*/
                        break;
                    } else {
                        continue;
                    }
                }
                if (cluster.size() == workersUrl.length) {
                    consumer.accept(cluster);
                }
            }
        }
    }

    @Override
    public void send(ChannelHandlerContext ctx, String msg) {
        ChannelFuture channelFuture = ctx.writeAndFlush(msg + MSG_TAIL);
        if(channelFuture.isDone()){
            System.out.println("send success!");
        }
        System.out.println(channelFuture.cause());
    }

    @Override
    public void register(ChannelHandlerContext ctx) {

    }
}
