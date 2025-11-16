package gtemp.gtemp_io.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.TemplateImage;
import gtemp.gtemp_io.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestPart("template") String templateJson,  // Use @RequestPart for JSON
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        try {
            System.out.println("=== USING @RequestPart ===");
            System.out.println("Raw JSON received: " + templateJson);
            System.out.println("Cover image present: " + (coverImage != null));

            // Parse JSON
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Template template = objectMapper.readValue(templateJson, Template.class);

            // Debug
            System.out.println("Template title: " + template.getTemplateTitle());
            System.out.println("Template engine: " + template.getEngine());

            // Handle cover image
            if (coverImage != null && !coverImage.isEmpty()) {
                String coverImagePath = "uploads/" + coverImage.getOriginalFilename();
                Files.createDirectories(Paths.get("uploads/"));
                Files.copy(coverImage.getInputStream(), Paths.get(coverImagePath));
                template.setCoverImagePath(coverImagePath);
                System.out.println("✓ Cover image saved: " + coverImagePath);
            }

            // Save template
            Template savedTemplate = templateService.createTemplate(template, coverImage, images, files);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Template created successfully!");
            response.put("templateId", savedTemplate.getId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }


//    @GetMapping
//    public ResponseEntity<List<Template>> getAllTemplates() {
//        return ResponseEntity.ok(templateService.getAllTemplates());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Template> getTemplate(@PathVariable Long id) {
//        return ResponseEntity.ok(templateService.getTemplateById(id));
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
//        templateService.deleteTemplate(id);
//        return ResponseEntity.noContent().build();
//    }

}
