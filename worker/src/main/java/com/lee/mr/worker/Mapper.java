package com.lee.mr.worker;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Mapper {
    ExecutorService executorService;
    public Mapper() {
        this.executorService = Executors.newFixedThreadPool(4, new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"map-thread-"+atomicInteger.getAndIncrement());
            }
        });
    }
    volatile boolean isRunning = false;
    public void map(String path, Consumer<String> consumer){
        CompletableFuture<List<String>> future = new CompletableFuture();
        Yaml yaml = new Yaml();
        Map<String,Object> map = yaml.loadAs(this.getClass().getResourceAsStream("/worker.yml"),Map.class);
        Map<String, Integer> server = (Map<String, Integer>) map.get("server");
        int workPort = server.get("port");
        InetAddress ip4 = null;
        try {
            ip4 = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String outDir = (String)map.get("map-out-dir");
        InetAddress finalIp = ip4;
        executorService.execute(() -> {
            isRunning = true;
            Map<String,BufferedWriter> writerMap = new HashMap<>();
            try {
                Map<String,Integer> words = new HashMap<>();
                BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
                String line = "";
                while((line=reader.readLine())!=null){
                    getWords(words,line);
                }
                if(finalIp !=null){
                    String hostAddress = finalIp.getHostAddress();
                    for (Map.Entry<String,Integer> entry:words.entrySet()){
                        String word = entry.getKey();
                        String first = word.substring(0, 1);
                        String filePath = outDir + File.separator + hostAddress + "-" + first.toUpperCase()+".txt";
                        File out = new File(filePath);
                        BufferedWriter writer = null;
                        if(out.exists()){
                            writer = writerMap.get(filePath);
                        }else {
                            writer = new BufferedWriter(new FileWriter(out));
                        }
                        writerMap.putIfAbsent(filePath,writer);
                        writer.write(word+" "+entry.getValue()+"\n");
                        writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                isRunning = false;
                consumer.accept(outDir);
            }
        });
    }

    public boolean isRunning(){
        return isRunning;
    }

    private void getWords(Map<String,Integer> map,String line){
        char[] chars = line.trim().toCharArray();
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

    private boolean isCharacter(char c){
        /*if(c==' '||c=='\t'||c=='\n'||c=='\r'){
            return true;
        }*/
        if((c>='a'&&c<='z')||(c>='A'&&c<='Z'))
            return true;
        return false;
    }
}
