package com.lee.mr.master.cluster;

import com.lee.mr.common.constant.ClientStatus;
import com.lee.mr.common.constant.Task;
import com.lee.mr.common.constant.URL;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MRCluster implements Cluster {
    Map<URL, ClientProxy> map = new ConcurrentHashMap<>();
    ClusterInvoker invoker = new ClusterInvoker();
    AtomicInteger count = new AtomicInteger();
    volatile List<ClientProxy> clients = Collections.synchronizedList(new ArrayList<>());
    @Override
    public void join(URL url, ClientProxy client) {
        map.put(url,client);
        clients.add(client);
    }

    @Override
    public boolean contains(URL url) {
        return map.containsKey(url);
    }

    @Override
    public boolean remove(URL url) {
        ClientProxy clientProxy = map.remove(url);
        synchronized (clients) {
            clients.remove(clientProxy);
        }
        return clientProxy !=null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void dispatch(Queue<Task> tasks) {
        while (!tasks.isEmpty()){
            synchronized (clients) {
                if (clients.size() > 0) {
                    int size = clients.size();
                    int i = count.getAcquire() % size;
                    ClientProxy client = clients.get(i);
                    if (client.isAlive() && !client.getStatus().equals(ClientStatus.RUNNING)) {
                        Task task = tasks.poll();
                        if (task != null) {
                            invoker.invoker(client, task);
                            client.setStatus(ClientStatus.RUNNING);
                            count.getAndIncrement();
                        }
                    }
                }
            }

        }

    }


}
