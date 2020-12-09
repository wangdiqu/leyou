package com.leyou.order.web.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.web.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties({UploadProperties.class})
public class UploadService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private UploadProperties uploadProperties;
    //定义允许类型
    //private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpg", "image/png", "image/bmp","image/jpeg");

    public String uploadImage(MultipartFile file) {

        try {
            //校验文件类型
            String contentType = file.getContentType();//获取文件类型
            if (!uploadProperties.getAllowTypes().contains(contentType)) {
                throw new LyException(ExceptionEnum.IMAGE_TYPE_ERROR);
            }
            //校验文件内容
            BufferedImage imageIo= ImageIO.read(file.getInputStream());
            if (imageIo == null) {//文件内容为null
                throw new LyException(ExceptionEnum.IMAGE_CONTENT_ERROR);
            }
            //获取后缀   substringAfterLast在最后一个点（"."）
            String extension= StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            //上传到fastDFS
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            //还可验证宽高
            //File f = new File("D:/Download/upload", file.getOriginalFilename());
            //保存文件到本地
            //file.transferTo(f);
            //返回路径
            return uploadProperties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("图片上传失败", e);
            throw new LyException(ExceptionEnum.IMAGE_ERROR);
        }
    }
}
