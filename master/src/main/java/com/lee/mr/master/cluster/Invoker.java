package com.lee.mr.master.cluster;

import com.lee.mr.common.constant.Task;

public interface Invoker {
    void invoker(ClientProxy client, Task task);
}
