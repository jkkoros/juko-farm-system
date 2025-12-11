package com.example.farmmanagement.controller;

import com.example.farmmanagement.model.Farm;
import com.example.farmmanagement.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class UploadsController {

    @Autowired
    private FarmService farmService;  // ← THIS WAS MISSING!

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @PostMapping("/farms/upload-photo/{id}")
    public String uploadPhoto(@PathVariable Long id, @RequestParam("photo") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Now this works — farmService is injected!
                Farm farm = farmService.getFarmById(id);
                if (farm != null) {
                    farm.setPhoto(fileName);
                    farmService.saveFarm(farm);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/farms";
    }
}