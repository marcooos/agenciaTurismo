package com.dev.turismo.agencia.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadRoot) throws IOException {
        this.root = Paths.get(uploadRoot).toAbsolutePath().normalize();
        Files.createDirectories(this.root.resolve("pacotes"));
    }

    public String savePacoteImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new IOException("Arquivo vazio");
        String ct = (file.getContentType() == null ? "" : file.getContentType()).toLowerCase();
        if (!ct.startsWith("image/"))
            throw new IOException("Apenas imagem");
        String name = UUID.randomUUID().toString().replace("-", "");

        String ext = "";
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int i = original.lastIndexOf('.');
        if (i > 0)
            ext = original.substring(i).toLowerCase();

        Path target = root.resolve("pacotes").resolve(name + ext);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/files/pacotes/" + target.getFileName(); // URL pública
    }
}
