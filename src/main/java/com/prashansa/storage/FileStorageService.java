package com.prashansa.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        try {
            this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize upload directory", e);
        }
    }

    public String storeMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media file is required");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        if (!contentType.startsWith("video/") && !contentType.startsWith("audio/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only video and audio files are allowed");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidence";
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0 && idx < original.length() - 1) {
            ext = original.substring(idx);
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadRoot.resolve(filename).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }
        return "/api/files/" + filename;
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        if (!contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "profile";
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0 && idx < original.length() - 1) {
            ext = original.substring(idx);
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadRoot.resolve(filename).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store image");
        }
        return "/api/files/" + filename;
    }

    public Resource load(String filename) {
        try {
            Path target = uploadRoot.resolve(filename).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }
            return resource;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load file");
        }
    }
}
