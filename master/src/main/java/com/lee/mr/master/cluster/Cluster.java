package com.lee.mr.master.cluster;

import com.lee.mr.common.constant.Task;
import com.lee.mr.common.constant.URL;

import java.util.Queue;

public interface Cluster {

    void join(URL url, ClientProxy client);
    boolean contains(URL url);
    boolean remove(URL url);
    int size();
    void dispatch(Queue<Task> tasks);
}
