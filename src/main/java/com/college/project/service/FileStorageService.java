package com.college.project.service;

import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Service managing disk storage and streaming retrieval of project documents,
 * synopses, and milestone deliverable files.
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.fileStorageLocation.resolve("proposals"));
            Files.createDirectories(this.fileStorageLocation.resolve("submissions"));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create directory for upload storage: " + ex.getMessage(), ex);
        }
    }

    /**
     * Saves an uploaded MultipartFile into a specific subfolder.
     *
     * @param file     The uploaded file
     * @param subDir   Target subfolder (e.g. "proposals", "submissions")
     * @return Relative storage path
     */
    public String storeFile(MultipartFile file, String subDir) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Security check against path traversal
        if (originalFileName.contains("..")) {
            throw new BadRequestException("Filename contains invalid relative path sequence: " + originalFileName);
        }

        // Generate collision-safe unique filename
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");

        try {
            Path targetLocation = this.fileStorageLocation.resolve(subDir).resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDir + "/" + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + uniqueFileName + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Loads a file from disk as a Spring Resource for download streaming.
     */
    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or unreadable: " + relativePath);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + relativePath);
        }
    }
}
