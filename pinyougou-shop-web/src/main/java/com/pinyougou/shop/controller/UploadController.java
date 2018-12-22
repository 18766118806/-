package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.FastDFSClient;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/18 21:14
 **/
@RestController
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String file_server_url;

    @RequestMapping("upload")
    public Result upload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename ();//全文件名
            String extName = originalFilename.substring (originalFilename.indexOf (".") + 1);
            FastDFSClient client = new FastDFSClient ("classpath:config/fdfs_client.conf");
            String url = client.uploadFile (file.getBytes (), extName);//参数( 文件内容,扩展名)
            url = file_server_url + url;
            return new Result (true, url);
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "上传失败");
        }
    }
}
