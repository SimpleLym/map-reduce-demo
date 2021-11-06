package com.lee.mr.master.cluster;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.Constant;
import com.lee.mr.common.constant.Task;

public class ClusterInvoker implements Invoker {

    @Override
    public void invoke(ClientProxy client, Task task) {
        if(client.isAlive()){
            JSONObject json = new JSONObject();
            json.put("task",task);
            json.put("code",0);
            client.send(json.toJSONString());
        }
    }
}
