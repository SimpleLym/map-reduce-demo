package com.lee.mr.worker;

import com.lee.mr.common.api.EventLoop;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientEventLoop implements EventLoop {
    ExecutorService executor;
    Dispatcher dispatcher;
    Queue<WrapTask> tasks;
    public ClientEventLoop(Queue<WrapTask> tasks,Dispatcher dispatcher) {
        executor = Executors.newSingleThreadExecutor();
        this.dispatcher = dispatcher;
        this.tasks = tasks;
    }

    @Override
    public void loop() {
        System.out.println("start loop...");
        executor.execute(() -> {
            while (true) {
                if (tasks != null && !tasks.isEmpty()) {
                    WrapTask wrapTask = tasks.poll();
                    dispatcher.dispatch(wrapTask.getTask(),wrapTask.getChannelHandlerContext());
                }
            }
        });
    }
}
