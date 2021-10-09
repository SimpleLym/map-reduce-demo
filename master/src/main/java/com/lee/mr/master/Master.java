package com.lee.mr.master;

import com.lee.mr.common.constant.Task;
import com.lee.mr.common.split.DefaultDataSplit;
import com.lee.mr.common.split.IDataSplit;
import com.lee.mr.common.split.InputSplit;
import com.lee.mr.master.cluster.MRCluster;
import com.lee.mr.master.handler.MasterChannelHandler;
import com.lee.mr.remoting.transport.api.Server;
import com.lee.mr.remoting.transport.netty4.NettyServer;
import org.yaml.snakeyaml.Yaml;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Master {
    public static void main(String[] args) {
        //new Master().getBindAddress();
        new Master().start();
    }

    public void start(){
        SocketAddress bindAddress = getBindAddress();
        String[] workersUrl = getWorkersUrl();
        MRCluster cluster = new MRCluster();
        MasterChannelHandler channelHandler = new MasterChannelHandler(workersUrl,cluster,cl->{
            Queue<Task> splitTasks = getSplitTasks();
            cl.dispatch(splitTasks);
        });
        Server server = new NettyServer(bindAddress,channelHandler);
        server.openServer();
    }

    public SocketAddress getBindAddress(){
        Yaml yaml = new Yaml();
        Map<String,Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/mapreduce.yml"),Map.class);
        String s = ((Map<String, String>) map.get("master")).get("url");
        System.out.println(s);
        String ip = s.split(":")[0];
        int port = Integer.parseInt(s.split(":")[1]);
        InetSocketAddress socketAddress = new InetSocketAddress(ip,port);
        return socketAddress;
    }

    public String[] getWorkersUrl(){
        Yaml yaml = new Yaml();
        Map<String,Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/mapreduce.yml"),Map.class);
        List<String> s = ((Map<String, List>) map.get("worker")).get("urls");
        if(s==null){
            return null;
        }
        return s.toArray(new String[s.size()]);
    }

    private Queue<Task> getSplitTasks(){
        Yaml yaml = new Yaml();
        Map<String,Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/mapreduce.yml"),Map.class);
        String s = ((String)map.get("file-dir"));
        IDataSplit dataSplit = new DefaultDataSplit(s);
        List<InputSplit> splits = dataSplit.getSplits();
        List<Task> tasks = splits.stream().map(i -> new Task("map", i.getFilePath())).collect(Collectors.toList());
        Queue queue = new LinkedBlockingQueue();
        queue.addAll(tasks);
        return queue;
    }
}
