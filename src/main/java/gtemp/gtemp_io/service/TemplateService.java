package gtemp.gtemp_io.service;

import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.File;
import gtemp.gtemp_io.repository.TemplateRepository;
import gtemp.gtemp_io.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    public Template createTemplate(Template template, List<MultipartFile> files) {
        template.setReleaseDate(LocalDateTime.now());
        template.setUpdateDate(LocalDateTime.now());

        template.setViews(0);
        template.setRating(0);
        template.setAverageRating(0);
        template.setDownloads(0);
        template.setWishlistCount(0);
        template.setWishlisted(false);
        template.setRevenue(0);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String storedFileName = fileService.storeFile(file);

                    File fileEntity = new File();
                    fileEntity.setFileName(file.getOriginalFilename());
                    fileEntity.setFilePath(storedFileName);
                    fileEntity.setFileType(file.getContentType());
                    fileEntity.setFileSize(file.getSize());
                    fileEntity.setTemplate(template);

                    template.getFiles().add(fileEntity);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                }
            }
        }

        return templateRepository.save(template);
    }

    public Template updateTemplate(Long id, Template templateDetails, List<MultipartFile> newFiles) {
        Template existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        existingTemplate.setTemplateTitle(templateDetails.getTemplateTitle());
        existingTemplate.setPrice(templateDetails.getPrice());
        existingTemplate.setTemplateDesc(templateDetails.getTemplateDesc());
        existingTemplate.setVisibility(templateDetails.isVisibility());
        existingTemplate.setEngine(templateDetails.getEngine());
        existingTemplate.setType(templateDetails.getType());
        existingTemplate.setTemplateImagePath(templateDetails.getTemplateImagePath());

        existingTemplate.setUpdateDate(LocalDateTime.now());

        if (newFiles != null && !newFiles.isEmpty()) {
            List<File> additionalFiles = uploadFiles(newFiles, existingTemplate);
            existingTemplate.getFiles().addAll(additionalFiles);
        }

        return templateRepository.save(existingTemplate);
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Template getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
    }

    public void deleteTemplate(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        deleteTemplateFiles(template);

        templateRepository.delete(template);
    }

    public List<Template> getVisibleTemplates() {
        return templateRepository.findAll().stream()
                .filter(Template::isVisibility)
                .toList();
    }

    private List<File> uploadFiles(List<MultipartFile> files, Template template) {
        List<File> fileEntities = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String storedFileName = fileService.storeFile(file);

                File fileEntity = new File();
                fileEntity.setFileName(file.getOriginalFilename());
                fileEntity.setFilePath(storedFileName);
                fileEntity.setFileType(file.getContentType());
                fileEntity.setFileSize(file.getSize());
                fileEntity.setTemplate(template);

                fileEntities.add(fileEntity);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
            }
        }

        return fileRepository.saveAll(fileEntities);
    }

    private void deleteTemplateFiles(Template template) {
        if (template.getFiles() != null) {
            for (File file : template.getFiles()) {
                try {
                    fileService.deleteFile(file.getFilePath());
                } catch (IOException e) {
                    System.err.println("Failed to delete file: " + file.getFilePath());
                }
            }
        }
    }

    public byte[] downloadFile(String filename) throws IOException {
        return fileService.loadFile(filename);
    }

    public String uploadTemplateImage(MultipartFile imageFile) throws IOException {
        if (!fileService.isImageFile(imageFile)) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (!fileService.isFileSizeValid(imageFile, 5 * 1024 * 1024)) {
            throw new IllegalArgumentException("File size too large");
        }

        return fileService.storeFile(imageFile);
    }
}