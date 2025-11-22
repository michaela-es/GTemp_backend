package gtemp.gtemp_io.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private final String STORAGE_DIR = "uploads";

    public String saveCoverImage(MultipartFile file, Long templateId) throws IOException {
        return saveFile(file, "covers", templateId);
    }

    public String saveTemplateImage(MultipartFile file, Long templateId) throws IOException {
        return saveFile(file, "images", templateId);
    }

    public String saveTemplateFile(MultipartFile file, Long templateId) throws IOException {
        return saveFile(file, "template_files", templateId);
    }

    private String saveFile(MultipartFile file, String subDir, Long templateId) throws IOException {
        // If templateId is null, just use subDir
        Path dirPath = templateId != null
                ? Paths.get(STORAGE_DIR, subDir, "template_" + templateId)
                : Paths.get(STORAGE_DIR, subDir, "temp");

        // Create directories if they do not exist
        Files.createDirectories(dirPath);

        // Use UUID to avoid filename conflicts
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(filename);

        // Save the file
        file.transferTo(filePath.toFile());

        // Return relative path (e.g., "uploads/covers/template_1/uuid_file.png")
        return STORAGE_DIR + "/" + subDir + "/" + (templateId != null ? "template_" + templateId : "temp") + "/" + filename;
    }

    public void deleteFile(String path) throws IOException {
        if (path != null && !path.isEmpty()) {
            Files.deleteIfExists(Paths.get(path));
        }
    }

    public boolean isImageFile(MultipartFile file) {
        return file.getContentType() != null && file.getContentType().startsWith("image/");
    }

    public boolean isFileSizeValid(MultipartFile file, long maxSize) {
        return file != null && file.getSize() <= maxSize;
    }

    public byte[] loadFile(String path) throws IOException {
        return path != null ? Files.readAllBytes(Paths.get(path)) : new byte[0];
    }
}
