package gtemp.gtemp_io.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetLocation);

        return storedFileName;
    }

    public byte[] loadFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + filename);
        }
        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Files.deleteIfExists(filePath);
    }

    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isFileSizeValid(MultipartFile file, long maxSizeInBytes) {
        return file.getSize() <= maxSizeInBytes;
    }
}