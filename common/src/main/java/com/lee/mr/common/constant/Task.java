package com.lee.mr.common.constant;

import java.util.UUID;

public class Task {
    String id;
    String type;
    String[] filePaths;

    public Task() {
    }

    public Task(String type, String[] filePaths) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.filePaths = filePaths;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(String[] filePaths) {
        this.filePaths = filePaths;
    }
}
