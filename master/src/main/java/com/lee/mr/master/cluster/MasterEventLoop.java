package com.lee.mr.master.cluster;

import com.lee.mr.common.api.EventLoop;
import com.lee.mr.common.constant.Task;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 事件轮询
 * */
public class MasterEventLoop implements EventLoop {
    ExecutorService executor;
    Queue<Task> tasks;
    Cluster cluster;

    public MasterEventLoop(Queue<Task> tasks, Cluster cluster) {
        executor = Executors.newSingleThreadExecutor();
        this.tasks = tasks;
        this.cluster = cluster;
    }
    @Override
    public void loop() {
        System.out.println("start loop...");
        executor.execute(() -> {
            while (true) {
                if (tasks != null && !tasks.isEmpty()) {
                    //if (cluster.size() > 0) {
                    if (cluster.size() == 2) {
                        cluster.dispatch(tasks);
                    }
                }
            }
        });
    }
}
