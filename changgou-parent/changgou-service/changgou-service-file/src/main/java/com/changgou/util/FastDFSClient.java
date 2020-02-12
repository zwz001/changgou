package com.changgou.util;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class FastDFSClient {
    static{
        try {
            //获取类路径下的配置文件
            ClassPathResource classPathResource = new ClassPathResource("fdfs_client.conf");
            ClientGlobal.init(classPathResource.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //上传
    public static String[] upload(FastDFSFile file) throws Exception{
        //1.创建一个配置文件，配置服务器的地址（ip和port）
        //2.加载配置文件
        //3.创建trackerClient对象
        TrackerClient trackerClient=new TrackerClient();
        //4.获取trackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //5.创建storageServer对象
        StorageServer storageServer = null;
        //6.创建storageClient对象（操作图片的API CURD）上传图片即可
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        //参数1 指定文件的字节数组
        //参数2 指定图片的扩展名，不要带点
        //参数3 指定图片的元数据 比如：像素大小图片的大小拍摄日期，拍摄的作者
        NameValuePair[] meta_List = new NameValuePair[]{
                new NameValuePair(file.getAuthor()),
                new NameValuePair(file.getName()),
                new NameValuePair(file.getSize())
        };
        String[] jpgs = storageClient.upload_file(file.getContent(), file.getExt(), meta_List);
        return jpgs;
    }

    //下载
    public static byte[] downFile(String groupName,String remoteFileName) throws Exception{
        //1.创建一个配置文件，配置服务器的地址（ip和port）
        //2.加载配置文件
        //3.创建trackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //4.获取trackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //5.创建storageServer对象
        StorageServer storageServer=null;
        //6.创建storageClient对象（操作图片的API CURD）下载
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        //指定祖名
        //指定文件名
        byte[] groupls = storageClient.download_file(groupName, remoteFileName);
        return groupls;
    }
    public static int deleteFile(String groupName,String remoteFileName) throws Exception{
        //1.创建一个配置文件，配置服务器的地址（ip和port）
        //2.加载配置文件
        //3.创建trackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //4.获取trackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //5.创建storageServer对象
        StorageServer storageServer=null;
        //6.创建storageClient对象
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        int group1 = storageClient.delete_file(groupName, remoteFileName);
        if (group1==0){
            System.out.println("成功");
        }else{
            System.out.println("失败");
        }
        return group1;
    }
    public static String getTrackerUrl(){
        try {
            //创建TrackerClient对象
            TrackerClient trackerClient = new TrackerClient();
            //通过TrackerClient获取TrackerServer对象
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Tracker地址
            return "http://"+trackerServer.getInetSocketAddress().getHostString()+":"+ClientGlobal.getG_tracker_http_port();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
