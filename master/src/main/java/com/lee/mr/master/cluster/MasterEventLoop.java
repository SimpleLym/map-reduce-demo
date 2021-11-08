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
    String[] workersUrl;
    public MasterEventLoop(Queue<Task> tasks, Cluster cluster,String[] workersUrl) {
        executor = Executors.newSingleThreadExecutor();
        this.tasks = tasks;
        this.cluster = cluster;
        this.workersUrl = workersUrl;
    }
    @Override
    public void loop() {
        System.out.println("start loop...");
        executor.execute(() -> {
            while (true) {
                if (tasks != null && !tasks.isEmpty()) {
                    if (cluster.size() == this.workersUrl.length) {
                        cluster.dispatch(tasks);
                    }
                }
            }
        });
    }
}
