package com.changgou.controller;

import com.changgou.util.FastDFSClient;
import com.changgou.util.FastDFSFile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class UploadController {
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file){
        try {
            if(!file.isEmpty()){
                //获取字节数组，获取原来的文件的名称，获取原来的文件的扩展名 存储到fastdfs上
                FastDFSFile fdfsfile = new FastDFSFile(
                        file.getOriginalFilename(),     //原文件名
                        file.getBytes(),    //字节数组
                        StringUtils.getFilenameExtension(file.getOriginalFilename())//得到jpg
                );
                String[] upload= FastDFSClient.upload(fdfsfile);
                //3.返回一个上传图片的路径，拼接字符串
                String realpath = FastDFSClient.getTrackerUrl() + "/" + upload[0] + "/" + upload[1];
                return realpath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
