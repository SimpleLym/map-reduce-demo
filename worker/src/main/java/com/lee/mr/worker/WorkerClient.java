package com.lee.mr.worker;

import com.lee.mr.common.api.EventLoop;
import com.lee.mr.common.constant.Task;
import com.lee.mr.common.constant.URL;
import com.lee.mr.remoting.transport.api.Client;
import com.lee.mr.remoting.transport.netty4.NettyClient;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkerClient {
    Client client;
    Map<String,String> master;
    //Map<String,Integer> clientServer;
    public static String clientUrlStr;
    Queue<WrapTask> tasks;
    WorkerClientHandler clientHandler;
    Dispatcher dispatcher;

    public WorkerClient() {
        Yaml yaml = new Yaml();
        Map<String,Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/worker.yml"),Map.class);
        master = (Map<String,String>)map.get("master");
        //clientServer = (Map<String,Integer>)map.get("server");
        tasks = new LinkedBlockingQueue<>();
        clientHandler = new WorkerClientHandler(tasks);
        dispatcher = new Dispatcher(clientHandler);
        EventLoop eventLoop = new ClientEventLoop(tasks,dispatcher);
        eventLoop.loop();
    }

    public static void main(String[] args) {
        System.out.println(args[0]);
        clientUrlStr = args[0];
        WorkerClient workerClient = new WorkerClient();
        workerClient.run();
    }

    public void run(){
        String strUrl = master.get("url");
        //Integer port = clientServer.get("port");
        URL masterUrl = new URL(strUrl);
        URL clientUrl = new URL(clientUrlStr);
        this.client = new NettyClient(masterUrl,clientUrl,clientHandler);
        clientHandler.setClient(client);
        boolean connect = this.client.connect();
        System.out.println("connect:"+connect);
    }

}
