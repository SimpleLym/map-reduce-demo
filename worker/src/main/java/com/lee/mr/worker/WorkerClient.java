package com.lee.mr.worker;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.URL;
import com.lee.mr.remoting.transport.api.Client;
import com.lee.mr.remoting.transport.netty4.NettyClient;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class WorkerClient {
    Client client;
    Map<String,String> master;
    Map<String,Integer> clientServer;
    public WorkerClient() {
        Yaml yaml = new Yaml();
        Map<String,Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/worker.yml"),Map.class);
        master = (Map<String,String>)map.get("master");
        clientServer = (Map<String,Integer>)map.get("server");
    }

    public static void main(String[] args) {
        WorkerClient workerClient = new WorkerClient();
        workerClient.run();
    }

    public void run(){
        String strUrl = master.get("url");
        Integer port = clientServer.get("port");
        URL masterUrl = new URL(strUrl);
        URL serverUrl = new URL("127.0.0.1:"+port);
        WorkerClientHandler clientHandler = new WorkerClientHandler();
        this.client = new NettyClient(masterUrl,serverUrl,clientHandler);
        boolean connect = this.client.connect();
        System.out.println("connect:"+connect);
        if(connect){
            JSONObject json = new JSONObject();
            json.put("type","init");
            json.put("code",0);
            this.client.send(json.toJSONString());
        }
    }

}
