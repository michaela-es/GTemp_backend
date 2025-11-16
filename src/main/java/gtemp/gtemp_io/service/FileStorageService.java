package gtemp.gtemp_io.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload folder", e);
        }
    }

    public String saveCoverImage(MultipartFile file, Long templateId) {
        return saveFileToFolder(file, "covers/template_" + templateId);
    }

    public String saveTemplateImage(MultipartFile file, Long templateId) {
        return saveFileToFolder(file, "images/template_" + templateId);
    }

    public String saveTemplateFile(MultipartFile file, Long templateId) {
        return saveFileToFolder(file, "files/template_" + templateId);
    }

    private String saveFileToFolder(MultipartFile file, String subfolder) {
        try {
            Path folderPath = rootDir.resolve(subfolder).normalize();
            Files.createDirectories(folderPath);
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = folderPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return subfolder + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error saving file to " + subfolder, e);
        }
    }

    public String storeFile(MultipartFile file) {
        return saveFileToFolder(file, "uploads");
    }

}
