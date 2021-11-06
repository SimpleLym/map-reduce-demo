package com.lee.mr.worker;

import com.lee.mr.common.constant.Task;
import io.netty.channel.ChannelHandlerContext;

public class WrapTask {
    ChannelHandlerContext channelHandlerContext;
    Task task;

    public WrapTask(ChannelHandlerContext channelHandlerContext, Task task) {
        this.channelHandlerContext = channelHandlerContext;
        this.task = task;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
