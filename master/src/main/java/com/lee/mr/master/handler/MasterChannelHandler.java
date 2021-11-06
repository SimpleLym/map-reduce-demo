package com.lee.mr.master.handler;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.ClientStatus;
import com.lee.mr.common.constant.Task;
import com.lee.mr.common.constant.URL;
import com.lee.mr.common.split.DefaultDataSplit;
import com.lee.mr.common.split.IDataSplit;
import com.lee.mr.common.split.InputSplit;
import com.lee.mr.master.cluster.MasterEventLoop;
import com.lee.mr.master.cluster.MRCluster;
import com.lee.mr.master.cluster.ClientProxy;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MasterChannelHandler implements ChannelHandler {
    String[] workersUrl;
    //Map<String,Channel> map;
    MRCluster cluster;
    AtomicInteger counter;
    Queue<Task> tasks;

    public MasterChannelHandler(String[] workersUrl, MRCluster cluster) {
        this.workersUrl = workersUrl;
        counter = new AtomicInteger(0);
        //this.map = new ConcurrentHashMap<>();
        this.cluster = cluster;
        tasks = getSplitTasks();
        MasterEventLoop masterEventLoop = new MasterEventLoop(tasks, cluster);
        masterEventLoop.loop();
    }

    @Override
    public void received(ChannelHandlerContext ctx, String msg) {
        System.out.println("receive:" + msg);
        JSONObject json = JSONObject.parseObject(msg);
        if (json != null) {
            String type = json.getString("type");
            int code = json.getInteger("code");
            if (code == 0 && "reduce".equals(type)) {
                String filePath = json.getString("filePath");
                File dir = new File(filePath);
                Map<Integer,List<String>> map = new HashMap<>();
                if(dir.isDirectory()){
                    File[] files = dir.listFiles();
                    for(File file:files){
                        if(file.isDirectory()){
                            File[] fs = file.listFiles();
                            for (File f:fs){
                                char c = f.getName().split("-")[1].charAt(0);
                                int num =  (int)c%workersUrl.length;
                                if(map.get(num)==null){
                                    map.put(num,new ArrayList<>());
                                }
                                List<String> list = map.get(num);
                                list.add(f.getAbsolutePath());
                            }
                        }
                    }
                }
                /*JSONObject message = new JSONObject();
                message.put("code",0);*/
                //message.put("filePaths",);
                for (int i=0;i<workersUrl.length;i++){
                    Task task = new Task();
                    task.setType("reduce");
                    task.setId(UUID.randomUUID().toString());
                    List<String> paths = map.get(i);
                    task.setFilePaths(paths.toArray(new String[paths.size()]));
                    tasks.add(task);
                }
            }else if(code==505){
                //客户端正忙，应该把任务重新放到队列中去
            }else if("finish".equals(json.getString("type"))){
                System.out.println(ctx.channel().remoteAddress()+" finish!");
            }
        }
    }

    private Queue<Task> getSplitTasks() {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/mapreduce.yml"), Map.class);
        String s = ((String) map.get("file-dir"));
        IDataSplit dataSplit = new DefaultDataSplit(s);
        List<InputSplit> splits = dataSplit.getSplits();
        List<Task> tasks = splits.stream().map(i -> new Task("map", new String[]{i.getFilePath()})).collect(Collectors.toList());
        Queue queue = new LinkedBlockingQueue();
        queue.addAll(tasks);
        return queue;
    }

    @Override
    public void send(ChannelHandlerContext ctx, String msg) {
        System.out.println(Thread.currentThread().getName());
        ChannelFuture channelFuture = ctx.writeAndFlush(msg + "\r\n");
        if (channelFuture.isDone()) {
            System.out.println("send success!");
        }
    }
    @Override
    public void register(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress() + " connected!");
        //注册上来的客户端必须是服务端提前配置好的
        //TODO 是否可以使用自动注册与发现？
        for (String url : workersUrl) {
            String ip = url.split(":")[0];
            int port = Integer.parseInt(url.split(":")[1]);
            URL url1 = new URL(ip, port);
            if (channel.remoteAddress()
                    .equals(new InetSocketAddress(ip, port))) {
                if (cluster.contains(url1)) {
                    cluster.remove(url1);
                }
                System.out.println(url1+" registered");
                ClientProxy client = new ClientProxy(ctx, this);
                client.setUrl(url1);
                client.setStatus(ClientStatus.CONNECTED);
                cluster.join(url1, client);
                break;
            } else {
                continue;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }
}
