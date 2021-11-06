package com.lee.mr.worker;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.Task;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import static com.lee.mr.common.constant.Constant.MSG_TAIL;

public class Dispatcher {
    Mapper mapper;
    Reducer reducer;
    ChannelHandler channelHandler;

    public Dispatcher(ChannelHandler channelHandler) {
        mapper = new Mapper();
        reducer = new Reducer();
        this.channelHandler = channelHandler;
    }

    public void dispatch(Task task, ChannelHandlerContext ctx){
        String[] filePaths = task.getFilePaths();
        if("map".equals(task.getType())){
            if(!mapper.isRunning()) {
                mapper.map(filePaths, paths -> {
                    JSONObject json = new JSONObject();
                    json.put("code", 0);
                    json.put("type", "reduce");
                    json.put("filePath", paths);
                    channelHandler.send(ctx,json.toJSONString());
                });
            }else {
                JSONObject json = new JSONObject();
                json.put("code",505);
                json.put("msg","mapper is running!please change other client!");
                json.put("type","map");
                json.put("task",task);
                ctx.writeAndFlush(json.toJSONString()+MSG_TAIL);
            }
        }else if("reduce".equals(task.getType())){
            System.out.println("start reduce...");
            if(!reducer.running.get()) {
                reducer.reduce(filePaths, path -> {
                    JSONObject json = new JSONObject();
                    json.put("code", 0);
                    json.put("type", "finish");
                    json.put("filePath", path);
                    ctx.writeAndFlush(json.toJSONString() + MSG_TAIL);
                });
            }else {
                JSONObject json = new JSONObject();
                json.put("code",505);
                json.put("msg","reducer is running!please change other client!");
                json.put("type","reduce");
                json.put("task",task);
                ctx.writeAndFlush(json.toJSONString()+MSG_TAIL);
            }
        }
    }

}
