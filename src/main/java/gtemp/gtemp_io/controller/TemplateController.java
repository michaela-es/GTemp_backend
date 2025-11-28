package gtemp.gtemp_io.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gtemp.gtemp_io.entity.File;
import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.repository.TemplateRepository;
import gtemp.gtemp_io.service.TemplateService;
import gtemp.gtemp_io.entity.User;
import gtemp.gtemp_io.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus; 

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "http://localhost:5173")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserService userService;

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
            }

            Template savedTemplate = templateService.createTemplate(template, coverImage, images, files);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Template created successfully!");
            response.put("templateId", savedTemplate.getId());

            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/debug-uploads")
    public ResponseEntity<Map<String, Object>> debugUploads() {
        Map<String, Object> debugInfo = new HashMap<>();
        try {
            String projectRoot = System.getProperty("user.dir");
            String uploadsPath = projectRoot + "/uploads/";
            debugInfo.put("projectRoot", projectRoot);
            debugInfo.put("uploadsAbsolutePath", uploadsPath);
            debugInfo.put("uploadsAbsoluteExists", Files.exists(Paths.get(uploadsPath)));

            if (Files.exists(Paths.get(uploadsPath))) {
                List<String> files = Files.list(Paths.get(uploadsPath))
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                debugInfo.put("filesInUploads", files);
            }
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
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable Long id) {
        try {
            Optional<Template> templateOpt = templateService.getTemplateById(id);
            if (templateOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Template not found"));

            return ResponseEntity.ok(templateOpt.get());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchaseTemplate(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) Double donationAmount
    ) {
        try {
            Optional<Template> templateOpt = templateService.getTemplateById(id);
            Optional<User> userOpt = userService.getUserById(userId);

            if (templateOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Template not found");
            if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

            Template template = templateOpt.get();
            User user = userOpt.get();

            double amountToDeduct = switch (template.getPriceSetting()) {
                case "Paid" -> template.getPrice() != null ? template.getPrice() : 0;
                case "₱0 or donation" -> donationAmount != null ? donationAmount : 0;
                case "No Payment" -> 0;
                default -> 0;
            };

            if (user.getWallet() < amountToDeduct) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Insufficient wallet balance");
            }

            // Subtract wallet and update revenue
            user.setWallet(user.getWallet() - amountToDeduct);
            userService.saveUser(user);

            template.setRevenue(template.getRevenue() + (float) amountToDeduct);
            templateService.saveTemplate(template);

            return ResponseEntity.ok(Map.of(
                    "message", "Purchase successful",
                    "deductedAmount", amountToDeduct
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Purchase failed: " + e.getMessage());
        }
    }


    /** 
     * New endpoint: download all files for Free template as a single ZIP 
     */
    @GetMapping("/{id}/download/free")
    public ResponseEntity<Resource> downloadFreeTemplate(@PathVariable Long id) throws IOException {
        Optional<Template> templateOpt = templateService.getTemplateById(id);
        if (templateOpt.isEmpty()) return ResponseEntity.notFound().build();

        Template template = templateOpt.get();
        List<File> files = template.getFiles();
        if (files == null || files.isEmpty()) return ResponseEntity.badRequest().build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (File file : files) {
                Path filePath = Paths.get(file.getFilePath()); // Adjust if your File entity uses another property
                if (!Files.exists(filePath)) continue;
                zos.putNextEntry(new ZipEntry(filePath.getFileName().toString()));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }
        }

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + template.getTemplateTitle() + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
