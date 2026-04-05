package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.Resp;
import com.ywtong.springboothtml.entity.equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ywtong.springboothtml.repository.EquipmentRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

@Service
public class service {
    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<equipment> searchEquipment(String name, String brand) {
        if (name == null) name = "";
        if (brand == null) brand = "";
        return equipmentRepository.findByNameLikeAndBrand(name, brand);
    }

    public equipment save(equipment entity) {
        return equipmentRepository.save(entity);
    }

    public void del(Long id) {
        if (id != null) {
            equipmentRepository.deleteById(id);
        }
    }

    public Resp<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Resp.fail("400", "文件为空");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return Resp.fail("400", "文件名不能为空");
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String fileName = System.currentTimeMillis() + "." + extension;
        String filePath = "D:\\Sping_file_temp\\";
        File dest = new File(filePath + fileName);

        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return Resp.fail("500", "创建目录失败");
            }
        }

        try {
            file.transferTo(dest);
            return Resp.success(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return Resp.fail("500", originalFileName + "上传失败: " + e.getMessage());
        }
    }
}
