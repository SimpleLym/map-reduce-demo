package com.lee.mr.worker;

import com.alibaba.fastjson.JSONObject;
import com.lee.mr.common.constant.Task;
import com.lee.mr.remoting.transport.api.ChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import static com.lee.mr.common.constant.Constant.MSG_TAIL;

public class WorkerClientHandler implements ChannelHandler {
    Dispatcher dispatcher;
    public WorkerClientHandler() {
        dispatcher = new Dispatcher();
    }

    @Override
    public void received(ChannelHandlerContext ctx, String msg) {
        Channel channel = ctx.channel();
        System.out.println("received "+msg);
        JSONObject jsonObject = JSONObject.parseObject(msg);
        Task task = jsonObject.getObject("task", Task.class);
        String filePath = task.getFilePath();
        dispatcher.dispatch(task,filePath,channel);
    }

    @Override
    public void send(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(msg+MSG_TAIL);
    }

    @Override
    public void register(ChannelHandlerContext ctx) {

    }
}
