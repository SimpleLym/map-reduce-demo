package com.lee.mr.common.constant;

import java.util.UUID;

public class Task {
    String id;
    String type;
    String filePath;

    public Task() {
    }

    public Task(String type, String filePath) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.filePath = filePath;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
