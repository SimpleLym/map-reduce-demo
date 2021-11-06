package com.lee.mr.worker;

import com.lee.mr.common.util.CommonUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Reducer {
    AtomicBoolean running = new AtomicBoolean();
    final String outPath = "D:\\workspace\\6.824\\out";
    int workPort;

    public Reducer() {
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(this.getClass().getResourceAsStream("/worker.yml"), Map.class);
        Map<String, Integer> server = (Map<String, Integer>) map.get("server");
        workPort = server.get("port");
    }

    public void reduce(String[] paths, Consumer<String> consumer) {
        running.set(true);
        Map<String, List<String>> map = new HashMap<>();
        try {
            for (String path : paths) {
                char c = path.substring(path.lastIndexOf("-")+1,path.lastIndexOf("-")+2).charAt(0);
                String s = (c + "").toLowerCase();
                List<String> list = map.getOrDefault(s, new ArrayList<>());
                list.add(path);
                map.put(s,list);
            }
            var ref = new Object() {
                String path = CommonUtil.getPathByCurrentIp(outPath,WorkerClient.clientUrlStr);
            };
            map.forEach((k, list) -> {
                Map<String, Integer> mergedMap = merge(list);
                String temp = ref.path;
                File file = new File(temp);
                if (!file.exists()) {
                    file.mkdir();
                }
                String filePath = ref.path + File.separator + WorkerClient.clientUrlStr.replaceAll(":","#") + "-" + k.toUpperCase() + ".txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)))) {
                    mergedMap.forEach((word, count) -> {
                        try {
                            writer.write(word + " " + count);
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            running.set(false);
            consumer.accept(outPath);
        }
    }

    private Map<String, Integer> merge(List<String> paths) {
        Map<String, Integer> map = new HashMap<>();
        for (String path : paths) {
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    String[] arr = line.split(" ");
                    String word = arr[0];
                    int count = Integer.parseInt(arr[1]);
                    int c = map.getOrDefault(word, 0);
                    c = c + count;
                    map.put(word, c);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
