package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.service.TemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "http://localhost:5173")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String UPLOAD_DIR = "uploads/";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestParam("template") String templateJson,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        try {
            Template template = objectMapper.readValue(templateJson, Template.class);
            Template savedTemplate = templateService.createTemplate(template, files);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Template created successfully");
            response.put("templateId", savedTemplate.getId());
            response.put("templateTitle", savedTemplate.getTemplateTitle());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates() {
        List<Template> templates = templateService.getAllTemplates();
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplateById(@PathVariable Long id) {
        try {
            Template template = templateService.getTemplateById(id);
            return new ResponseEntity<>(template, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Template> updateTemplate(
            @PathVariable Long id,
            @RequestPart("template") Template templateDetails,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        try {
            if (files != null) {
                for (MultipartFile multipartFile : files) {
                    String filePath = UPLOAD_DIR + multipartFile.getOriginalFilename();
                    Files.createDirectories(Paths.get(UPLOAD_DIR));
                    Files.copy(multipartFile.getInputStream(), Paths.get(filePath));

                    gtemp.gtemp_io.entity.File fileEntity = new gtemp.gtemp_io.entity.File();
                    fileEntity.setFilePath(filePath);
                    fileEntity.setTemplate(templateDetails);

                    templateDetails.addFile(fileEntity);
                }
            }

            Template updatedTemplate = templateService.updateTemplate(id, templateDetails, null);

            return new ResponseEntity<>(updatedTemplate, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(UPLOAD_DIR + filename));

            String contentType = Files.probeContentType(Paths.get(UPLOAD_DIR + filename));
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<Template>> getVisibleTemplates() {
        return new ResponseEntity<>(templateService.getVisibleTemplates(), HttpStatus.OK);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        try {
            Template template = templateService.getTemplateById(id);
            template.setViews(template.getViews() + 1);
            templateService.updateTemplate(id, template, null);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Template> rateTemplate(@PathVariable Long id, @RequestParam float rating) {
        try {
            Template template = templateService.getTemplateById(id);
            template.setRating(rating);
            Template updatedTemplate = templateService.updateTemplate(id, template, null);
            return new ResponseEntity<>(updatedTemplate, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<Void> incrementDownloads(@PathVariable Long id) {
        try {
            Template template = templateService.getTemplateById(id);
            template.setDownloads(template.getDownloads() + 1);
            templateService.updateTemplate(id, template, null);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
