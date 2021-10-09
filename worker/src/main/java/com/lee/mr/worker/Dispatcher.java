package com.lee.mr.worker;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.Task;
import io.netty.channel.Channel;

import static com.lee.mr.common.constant.Constant.MSG_TAIL;

public class Dispatcher {
    Mapper mapper;
    Reducer reducer;

    public Dispatcher() {
        mapper = new Mapper();
        reducer = new Reducer();
    }

    public void dispatch(Task task, String filePath, Channel channel){
        if("map".equals(task.getType())){
            if(!mapper.isRunning()) {
                mapper.map(filePath, paths -> {
                    JSONObject json = new JSONObject();
                    json.put("code", 0);
                    json.put("type", "map");
                    json.put("filePath", paths);
                    channel.writeAndFlush(json.toJSONString()+MSG_TAIL);
                });
            }else {
                JSONObject json = new JSONObject();
                json.put("code",505);
                json.put("type","map");
                json.put("filePath","");
                channel.writeAndFlush(json.toJSONString()+MSG_TAIL);
            }
        }else if("reduce".equals(task.getType())){
            reducer.reduce(channel,path->{
                JSONObject json = new JSONObject();
                json.put("code",0);
                json.put("type","reduce");
                json.put("filePath",path);
                channel.writeAndFlush(json.toJSONString()+MSG_TAIL);
            });
        }
    }

}
