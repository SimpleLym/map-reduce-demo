package com.lee.mr.worker;

import com.lee.mr.common.util.CommonUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Mapper {
    ExecutorService executorService;
    Map<String,Object> map;
    int workPort;
    public Mapper() {
        Yaml yaml = new Yaml();
        map = yaml.loadAs(this.getClass().getResourceAsStream("/worker.yml"),Map.class);
        Map<String, Integer> server = (Map<String, Integer>) map.get("server");
        workPort = server.get("port");

        this.executorService = Executors.newFixedThreadPool(4, new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"map-thread-"+atomicInteger.getAndIncrement());
            }
        });
    }
    AtomicBoolean isRunning = new AtomicBoolean();
    public void map(String[] paths, Consumer<String> consumer){
        //executorService.execute(() -> {
            isRunning.set(true);
            Map<String,BufferedWriter> writerMap = new HashMap<>();
            String outDir = (String)map.get("map-out-dir");
            outDir = CommonUtil.getPathByCurrentIp(outDir,WorkerClient.clientUrlStr);
            try {
                Map<String,Integer> words = new HashMap<>();
                for (String path:paths) {
                    File file = new File(path);
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        getWords(words, line);
                    }
                    String dir = outDir;
                    if(!new File(dir).exists()){
                        new File(dir).mkdir();
                    }
                    String hostAddress = WorkerClient.clientUrlStr;
                    if (hostAddress != null) {
                        dir = dir + File.separator + file.getName();
                        File directory = new File(dir);
                        if (!directory.exists()) {
                            directory.mkdir();
                        }
                        for (Map.Entry<String, Integer> entry : words.entrySet()) {
                            String word = entry.getKey();
                            String first = word.substring(0, 1);
                            String filePath = dir + File.separator + hostAddress.replaceAll(":","#") + "-" + first.toUpperCase() + ".txt";
                            File out = new File(filePath);
                            BufferedWriter writer = writerMap.get(filePath);
                            if (writer == null) {
                                writer = new BufferedWriter(new FileWriter(out));
                            }
                            writerMap.putIfAbsent(filePath, writer);
                            writer.write(word + " " + entry.getValue() + "\n");
                            writer.flush();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                isRunning.set(false);
                writerMap.forEach((k,w)->{
                    if(w!=null){
                        try {
                            w.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                writerMap.clear();
                System.out.println(Arrays.toString(paths)+" task finish!");
                consumer.accept(outDir);
            }
        //});
    }

    public boolean isRunning(){
        return isRunning.get();
    }

    private void getWords(Map<String,Integer> map,String line){
        char[] chars = line.trim().toCharArray();
        line = line.trim();
        int start = 0;
        for (int i=0;i<chars.length;i++){
            while (i<chars.length&&!isCharacter(chars[i])){
                i++;
            }
            start = i;
            if(i<chars.length) {
                while (i<chars.length&&isCharacter(chars[i])) {
                    i++;
                }
                //ma.add(line.substring(start,i));
                if(i<chars.length) {
                    String s = line.substring(start, i);
                    Integer count = map.get(s);
                    if (count == null) {
                        map.put(s, 1);
                    } else {
                        map.put(s, ++count);
                    }
                }
            }
        }
    }

    private static boolean isCharacter(char c){
        if((c>='a'&&c<='z')||(c>='A'&&c<='Z'))
            return true;
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isCharacter(','));
    }
}
