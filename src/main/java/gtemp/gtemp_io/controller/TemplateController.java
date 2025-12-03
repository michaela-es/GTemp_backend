package gtemp.gtemp_io.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.TemplateImage;
import gtemp.gtemp_io.repository.TemplateRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "http://localhost:5173")
public class TemplateController {

    @Autowired
    private TemplateService templateService;


    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestPart("template") String templateJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Template template = objectMapper.readValue(templateJson, Template.class);

            if (coverImage != null && !coverImage.isEmpty()) {
                String uploadsDir = "uploads/";

                Files.createDirectories(Paths.get(uploadsDir));

                String fileName = System.currentTimeMillis() + "_" +
                        coverImage.getOriginalFilename().replace(" ", "_");
                String fullFilePath = uploadsDir + fileName;

                Files.copy(coverImage.getInputStream(), Paths.get(fullFilePath));

                template.setCoverImagePath("uploads/" + fileName);

                System.out.println("✓ File saved to: " + fullFilePath);
                System.out.println("✓ Web path: uploads/" + fileName);

                boolean fileExists = Files.exists(Paths.get(fullFilePath));
                System.out.println("✓ File exists: " + fileExists);
            }

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

    @GetMapping("/debug-uploads")
    public ResponseEntity<Map<String, Object>> debugUploads() {
        Map<String, Object> debugInfo = new HashMap<>();

        try {
            String projectRoot = System.getProperty("user.dir");
            String uploadsPath = projectRoot + "/uploads/";
            String relativeUploadsPath = "uploads/";

            debugInfo.put("projectRoot", projectRoot);
            debugInfo.put("uploadsAbsolutePath", uploadsPath);
            debugInfo.put("uploadsRelativePath", relativeUploadsPath);

            boolean uploadsDirExists = Files.exists(Paths.get(uploadsPath));
            boolean relativeUploadsDirExists = Files.exists(Paths.get(relativeUploadsPath));

            debugInfo.put("uploadsAbsoluteExists", uploadsDirExists);
            debugInfo.put("uploadsRelativeExists", relativeUploadsDirExists);

            if (uploadsDirExists) {
                List<String> files = Files.list(Paths.get(uploadsPath))
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                debugInfo.put("filesInUploads", files);
            }

            String testFile = "1763289872903_kim-lip-can-you-entertain.jpg";
            boolean fileExistsAbsolute = Files.exists(Paths.get(uploadsPath + testFile));
            boolean fileExistsRelative = Files.exists(Paths.get(relativeUploadsPath + testFile));

            debugInfo.put("testFile", testFile);
            debugInfo.put("fileExistsAbsolute", fileExistsAbsolute);
            debugInfo.put("fileExistsRelative", fileExistsRelative);

            System.out.println("Debug uploads info: " + debugInfo);

        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
        }

        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Template>> getTemplatesByIds(@RequestBody List<Long> templateIds) {
        List<Template> templates = templateRepository.findAllById(templateIds);
        return ResponseEntity.ok(templates);
    }

    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates() {
        try {
            List<Template> templates = templateService.getAllTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{templateId}/images")
    public ResponseEntity<List<TemplateImage>> getTemplateImages(@PathVariable Long templateId) {
        List<TemplateImage> images = templateService.getTemplateImages(templateId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable Long id) {
        try {
            System.out.println("=== GET /api/templates/" + id + " called ===");

            Optional<Template> templateOpt = templateService.getTemplateById(id);

            if (templateOpt.isEmpty()) {
                System.out.println("Template not found with id: " + id);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Template not found with id: " + id);
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Template template = templateOpt.get();
            System.out.println("Found template: " + template.getTemplateTitle());
            System.out.println("Cover image path: " + template.getCoverImagePath());

            return ResponseEntity.ok(template);

        } catch (Exception e) {
            System.err.println("Error fetching template: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch template: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
