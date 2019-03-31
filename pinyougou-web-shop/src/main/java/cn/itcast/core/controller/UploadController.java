package cn.itcast.core.controller;

import cn.itcast.common.utils.FastDFSClient;
import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/*
* 图片上传
* */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String fsu;

    @RequestMapping("uploadFile")
    public Result uploadFile(MultipartFile file){

        try {
            //1、创建一个 FastDFS 的客户端 读取配置文件
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            //扩展名
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            //上传图片
            String path = fastDFSClient.uploadFile(file.getBytes(), extension, null);
            return new Result(true,fsu+path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }

}
