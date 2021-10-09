/**
 * @author ：lym
 * @date ：Created in 2021/6/18 14:12
 */
package com.lee.mr.common.split;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultDataSplit implements IDataSplit {
    String dirPath;
    public DefaultDataSplit(String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public List<InputSplit> getSplits() {
        File dir = new File(dirPath);
        List<InputSplit> list = new ArrayList<>();
        if(dir.exists()&&dir.isDirectory()){
            File[] files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith("pg-") && name.endsWith(".txt")) {
                        return true;
                    }
                    return false;
                }
            });
            list = Arrays.asList(files).stream().map(f -> new InputSplit(f.getAbsolutePath())).collect(Collectors.toList());
        }
        return list;
    }
}
