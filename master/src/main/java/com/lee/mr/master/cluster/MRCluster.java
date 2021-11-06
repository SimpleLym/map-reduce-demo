package com.lee.mr.master.cluster;

import com.lee.mr.common.constant.ClientStatus;
import com.lee.mr.common.constant.Task;
import com.lee.mr.common.constant.URL;
import com.lee.mr.remoting.transport.api.Client;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MRCluster implements Cluster {
    Map<URL, ClientProxy> map = new ConcurrentHashMap<>();
    ClusterInvoker invoker = new ClusterInvoker();
    AtomicInteger count = new AtomicInteger();
    volatile List<ClientProxy> clients = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<>());
    ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void join(URL url, ClientProxy client) {
        map.put(url, client);
        clients.add(client);
    }

    @Override
    public boolean contains(URL url) {
        return map.containsKey(url);
    }

    @Override
    public boolean remove(URL url) {
        ClientProxy clientProxy = map.remove(url);
        clients.remove(clientProxy);

        return clientProxy != null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void dispatch(Queue<Task> tasks) {
        System.out.println("dispatch task...");
        int size = clients.size();
        int i = count.getAcquire() % size;
        ClientProxy client = clients.get(i);
        if (client.isAlive() && !client.getStatus().equals(ClientStatus.RUNNING)) {
            Task task = tasks.poll();
            if (task != null) {
                executorService.execute(() -> {
                    try {
                        client.setStatus(ClientStatus.RUNNING);
                        invoker.invoke(client, task);
                        client.setStatus(ClientStatus.COMPLETE);
                        count.getAndIncrement();
                    } catch (Exception e) {
                        //TODO 重试策略。。。需要建立重试队列
                    }
                });
            }
        }
    }

}
