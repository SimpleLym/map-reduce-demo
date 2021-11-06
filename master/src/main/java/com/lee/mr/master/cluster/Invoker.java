package com.lee.mr.master.cluster;

import com.lee.mr.common.constant.Task;

public interface Invoker {
    void invoke(ClientProxy client, Task task);
}
