package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.Resp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:D:/upload/images/}")
    private String uploadPath;

    @Value("${upload.url-prefix:/demo/upload/images/}")
    private String urlPrefix;

    @PostMapping("/image")
    public Resp<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Resp.fail("400", "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Resp.fail("400", "文件名不能为空");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String allowedExtensions = ".jpg.jpeg.png.gif.bmp.webp";
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            return Resp.fail("400", "只支持图片格式：jpg, jpeg, png, gif, bmp, webp");
        }

        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadPath, filename);
            Files.write(filePath, file.getBytes());

            String fileUrl = urlPrefix + filename;
            return Resp.success(fileUrl);
        } catch (IOException e) {
            return Resp.fail("500", "文件上传失败：" + e.getMessage());
        }
    }
}
