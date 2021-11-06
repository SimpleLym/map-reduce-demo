package com.lee.mr.common.util;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommonUtil {
    public static String getPathByCurrentIp(String dir,String ip){
        String[] split = ip.split(":");
        dir += File.separator+split[0].replaceAll("\\.","")+"#"+split[1];
        return dir;
    }
}
