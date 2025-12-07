package gtemp.gtemp_io.service;

import gtemp.gtemp_io.entity.File;
import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.TemplateImage;
import gtemp.gtemp_io.repository.FileRepository;
import gtemp.gtemp_io.repository.TemplateImageRepository;
import gtemp.gtemp_io.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TemplateImageRepository templateImageRepository;

    // Folder where files/images will be saved
    private final String uploadDir = "uploads/";

    public Template createTemplate(Template template,
                                   MultipartFile coverImage,
                                   List<MultipartFile> images,
                                   List<MultipartFile> files) throws IOException {

        // Ensure lists are not null
        images = images != null ? images : List.of();
        files = files != null ? files : List.of();

        // 1️⃣ Save cover image
        if (coverImage != null && !coverImage.isEmpty()) {
            String coverImagePath = saveFileToDisk(coverImage);
            template.setCoverImagePath(coverImagePath);
        }

        // 2️⃣ Save template first
        Template savedTemplate = templateRepository.save(template);

        // 3️⃣ Save additional images
        for (MultipartFile imageFile : images) {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = saveFileToDisk(imageFile);

                TemplateImage templateImage = new TemplateImage();
                templateImage.setFileName(imageFile.getOriginalFilename());
                templateImage.setFilePath(imagePath);
                templateImage.setFileType(imageFile.getContentType());
                templateImage.setFileSize(imageFile.getSize());

                savedTemplate.addImage(templateImage); // sets template and adds to list
            }
        }

        // 4️⃣ Save additional files
        for (MultipartFile fileMultipart : files) {
            if (fileMultipart != null && !fileMultipart.isEmpty()) {
                String filePath = saveFileToDisk(fileMultipart);

                File fileEntity = new File();
                fileEntity.setFileName(fileMultipart.getOriginalFilename());
                fileEntity.setFilePath(filePath);
                fileEntity.setFileType(fileMultipart.getContentType());
                fileEntity.setFileSize(fileMultipart.getSize());

                savedTemplate.addFile(fileEntity); // sets template and adds to list
            }
        }

        // 5️⃣ Save everything
        return templateRepository.save(savedTemplate);
    }


    // Utility method to save files to disk
    private String saveFileToDisk(MultipartFile multipartFile) throws IOException {
        String filePath = Paths.get(uploadDir, System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename()).toString();

        java.io.File file = new java.io.File(filePath);
        file.getParentFile().mkdirs(); // ensure directories exist

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        return filePath;
    }

    public List<Template> getAllTemplates() {
        try {
            return templateRepository.findByVisibilityTrue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve templates: " + e.getMessage(), e);
        }
    }

    public Optional<Template> getTemplateById(Long id) {
        try {
            System.out.println("Service: Looking for template with ID: " + id);
            Optional<Template> template = templateRepository.findById(id);

            if (template.isPresent()) {
                System.out.println("Service: Found template - " + template.get().getTemplateTitle());
            } else {
                System.out.println("Service: No template found with ID: " + id);
            }

            return template;
        } catch (Exception e) {
            System.err.println("Error in getTemplateById: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Template saveTemplate(Template template) {
        return templateRepository.save(template);
    }

    public Template saveTemplateFiles(Template template, List<MultipartFile> images, List<MultipartFile> files) throws IOException {
        Template savedTemplate = templateRepository.save(template);

        if (images != null && !images.isEmpty()) {
            List<File> imageFiles = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String uploadsDir = "uploads/";
                    Files.createDirectories(Paths.get(uploadsDir));
                    String fileName = System.currentTimeMillis() + "_" +
                            image.getOriginalFilename().replace(" ", "_");
                    String fullFilePath = uploadsDir + fileName;
                    Files.copy(image.getInputStream(), Paths.get(fullFilePath));

                    File imageFile = new File();
                    imageFile.setFileName(image.getOriginalFilename());
                    imageFile.setFilePath("uploads/" + fileName);
                    imageFile.setFileSize(image.getSize());
                    imageFile.setFileType(image.getContentType());
                    imageFile.setTemplate(savedTemplate);
                    imageFiles.add(imageFile);
                }
            }
            fileRepository.saveAll(imageFiles);
        }

        if (files != null && !files.isEmpty()) {
            List<File> templateFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String uploadsDir = "uploads/";
                    Files.createDirectories(Paths.get(uploadsDir));
                    String fileName = System.currentTimeMillis() + "_" +
                            file.getOriginalFilename().replace(" ", "_");
                    String fullFilePath = uploadsDir + fileName;
                    Files.copy(file.getInputStream(), Paths.get(fullFilePath));

                    File templateFile = new File();
                    templateFile.setFileName(file.getOriginalFilename());
                    templateFile.setFilePath("uploads/" + fileName);
                    templateFile.setFileSize(file.getSize());
                    templateFile.setFileType(file.getContentType());
                    templateFile.setTemplate(savedTemplate); // Set the saved template
                    templateFiles.add(templateFile);
                }
            }
            fileRepository.saveAll(templateFiles);
        }

        return savedTemplate;
    }

    public List<Template> getTemplatesByOwner(Long ownerId) {
        try {
            return templateRepository.findByTemplateOwnerAndVisibilityTrue(ownerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve templates: " + e.getMessage(), e);
        }
    }
    

}
