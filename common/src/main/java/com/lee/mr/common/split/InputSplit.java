/**
 * @author ：lym
 * @date ：Created in 2021/6/18 13:48
 */
package com.lee.mr.common.split;

public class InputSplit {
    String filePath;

    public InputSplit() {
    }

    public InputSplit(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
