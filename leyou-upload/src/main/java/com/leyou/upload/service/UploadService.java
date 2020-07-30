package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.upload.controller.UploadController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author wyk
 * @date 2020/7/21 19:51
 */

@Service
public class UploadService {


    //定义log日志，用来输出查看错误原因
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);
    // 支持的文件类型
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/png", "image/jpeg");

    //注入fastdsf客户端
    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {

        // 1、图片信息校验
        String originalFilename = file.getOriginalFilename();
        //校验文件类型
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)) {
            //{} 是占位符，第二个参数填充此占位符
            LOGGER.info("文件类型不合法：{}" ,originalFilename);
            return null;
        }
        try {
            //校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                LOGGER.info("文件内容不合法：{}" ,originalFilename);
                return null;
            }
            //保存到文件的服务器
            // 2、保存图片
            // 2.1、生成保存目录  普通方式
//            File dir = new File("F:\\idea_doc\\springProject\\springcloud_vue_project\\image");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            // 2.2、保存图片
//            file.transferTo(new File(dir, originalFilename));
//            //返回url,进行回显 拼接图片地址
//            return "http://image.leyou.com/" + originalFilename;

            //使用FastDFS 文件存储系统
            //1，保存到服务器
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            //2， 生成url地址，返回
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部错误：" + originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
