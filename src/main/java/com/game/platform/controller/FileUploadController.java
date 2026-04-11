package com.game.platform.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class FileUploadController {

    private final String uploadDir = "uploads/";

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("File too large");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir + filename);

        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        String url = "http://localhost:8080/uploads/" + filename;

        return Map.of("url", url);
    }
}