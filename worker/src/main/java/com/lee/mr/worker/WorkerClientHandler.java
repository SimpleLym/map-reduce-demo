package com.lee.mr.worker;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.Task;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import com.lee.mr.remoting.transport.api.Client;
import io.netty.channel.ChannelHandlerContext;

import java.util.Queue;

import static com.lee.mr.common.constant.Constant.MSG_TAIL;

public class WorkerClientHandler implements ChannelHandler {
    Client client;
    Queue<WrapTask> wrapTasks;

    public WorkerClientHandler(Queue<WrapTask> tasks) {
        this.wrapTasks = tasks;
    }
    @Override
    public void received(ChannelHandlerContext ctx, String msg) {
        System.out.println("received "+msg);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        Task task = jsonObject.getObject("task", Task.class);
        WrapTask wrapTask = new WrapTask(ctx,task);
        wrapTasks.add(wrapTask);
    }

    @Override
    public void send(ChannelHandlerContext ctx, String msg) {
        System.out.println("client send:"+msg);
        ctx.writeAndFlush(msg + MSG_TAIL);
    }

    @Override
    public void register(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("client active");
        /*JSONObject json = new JSONObject();
        json.put("type","init");
        json.put("code",0);
        ctx.writeAndFlush(json.toJSONString() + MSG_TAIL);*/
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
