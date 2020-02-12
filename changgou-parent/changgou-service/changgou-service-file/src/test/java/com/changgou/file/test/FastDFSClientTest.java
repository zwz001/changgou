package com.changgou.file.test;

import org.csource.fastdfs.*;
import org.junit.Test;

public class FastDFSClientTest {
    @Test
    public void upload() throws Exception{
        //1.创建一个配置文件，配置服务器的地址（ip和port）
        //2.加载配置文件
        ClientGlobal.init("D:\\Project\\idea\\changgou\\changgou-parent\\changgou-service\\changgou-service-file\\src\\main\\resources\\fdfs_client.conf");
        //3.创建trackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //4.获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //5.创建storageServer对象
        StorageServer storageServer = null;
        //6/创建storageClient对象（操作图片的API CURD）上传图片即可
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        //参数1.指定图片的本地路径
        //参数2.指定图片的扩展名 不要带点
        //参数3.指定的图片的元数据，比如：像素大小，图片的大小，拍摄日期，拍摄的作者。。。
        String[] jpgs=storageClient.upload_file("D:\\复习资料\\a.jpg","jpg",null);
        //file_id
        for (String jpg : jpgs) {
            System.out.println(jpg);
        }
    }
}
