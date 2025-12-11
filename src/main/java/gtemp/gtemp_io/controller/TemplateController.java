package gtemp.gtemp_io.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gtemp.gtemp_io.dto.FileDTO;
import gtemp.gtemp_io.entity.*;
import gtemp.gtemp_io.repository.TemplateImageRepository;
import gtemp.gtemp_io.repository.TemplateRepository;
import gtemp.gtemp_io.security.jwt.JwtService;
import gtemp.gtemp_io.service.TemplateImageService;
import gtemp.gtemp_io.service.TemplateService;
import gtemp.gtemp_io.service.UserService;
import gtemp.gtemp_io.repository.PurchaseDownloadItemRepository;
import gtemp.gtemp_io.repository.RatingItemRepository;
import gtemp.gtemp_io.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import gtemp.gtemp_io.dto.TemplateHomePageDTO;

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
@CrossOrigin(origins = "*")
public class TemplateController {

    private final SecurityUtil securityUtil;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseDownloadItemRepository purchaseDownloadItemRepository;

    @Autowired
    private RatingItemRepository ratingItemRepository;

    @Autowired
    private TemplateImageService templateImageService;

    @Autowired
    TemplateImageRepository templateImageRepository;

    public TemplateController(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<?> getTemplateImages(@PathVariable Long id) {
        List<String> imagePaths = templateImageService.getImagePathsByTemplateId(id);

        if (imagePaths.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No images found for this template"));
        }

        return ResponseEntity.ok(imagePaths);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Template>> getTemplatesByIds(@RequestBody List<Long> templateIds) {
        List<Template> templates = templateRepository.findAllById(templateIds);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/homepage")
    public ResponseEntity<List<TemplateHomePageDTO>> getHomepageTemplates() {
        try {
            List<Template> templates = templateService.getAllTemplates();

            List<TemplateHomePageDTO> homepageDTOs = templates.stream()
                    .filter(template -> Boolean.TRUE.equals(template.getVisibility()))
                    .map(template -> new TemplateHomePageDTO(
                            template.getId(),
                            template.getTemplateTitle(),
                            template.getTemplateDesc(),
                            template.getCoverImagePath(),
                            template.getAverageRating(),
                            template.getPrice(),
                            template.getDownloadCount() != null ? template.getDownloadCount() : 0,
                            template.getEngine(),
                            template.getType()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(homepageDTOs);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable Long id) {
        Optional<Template> templateOpt = templateService.getTemplateById(id);
        if (templateOpt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Template not found"));

        Template template = templateOpt.get();

        if (Boolean.FALSE.equals(template.getVisibility())) {
            try {
                Long currentUserId = securityUtil.getCurrentUserId();

                if (!currentUserId.equals(template.getTemplateOwner())) {
                    return ResponseEntity.status(403).body(Map.of(
                            "error", "This template is private. Only the owner can view it."
                    ));
                }
            } catch (RuntimeException e) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "This template is private. Please login to view."
                ));
            }
        }

        if (template.getAverageRating() == null) {
            List<RatingItem> ratings = ratingItemRepository.findAll()
                    .stream()
                    .filter(r -> r.getTemplate().getId().equals(template.getId()))
                    .collect(Collectors.toList());

            double avg = ratings.stream().mapToInt(RatingItem::getRatingValue).average().orElse(0.0);
            template.setAverageRating(avg);
            templateService.saveTemplate(template);
        }

        if (template.getTemplateOwner() != null) {
            Optional<User> userOpt = userService.getUserById(template.getTemplateOwner());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String username = user.getUsername();
                template.setTemplateOwnerUsername(username);
                System.out.println("DEBUG: Found username: " + username + " for user ID: " + template.getTemplateOwner());
            } else {
                template.setTemplateOwnerUsername("Unknown User (ID: " + template.getTemplateOwner() + ")");
                System.out.println("DEBUG: No user found with ID: " + template.getTemplateOwner());
            }
        } else {
            template.setTemplateOwnerUsername("Unknown");
            System.out.println("DEBUG: templateOwner is null");
        }

        return ResponseEntity.ok(template);
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchaseTemplate(
            @PathVariable Long id,
            @RequestParam(required = false) Double donationAmount
    ) {
        try {
            Long userId = securityUtil.getCurrentUserId();

            Optional<Template> templateOpt = templateService.getTemplateById(id);
            Optional<User> userOpt = userService.getUserById(userId);

            if (templateOpt.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Template not found");
            if (userOpt.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

            Template template = templateOpt.get();
            User user = userOpt.get();

            List<PurchaseDownloadItem> existingItems = purchaseDownloadItemRepository.findByUserAndTemplate(user, template);

            Optional<PurchaseDownloadItem> purchasedOrDonated = existingItems.stream()
                    .filter(i -> i.getActionType() == PurchaseDownloadItem.ActionType.PURCHASED
                            || i.getActionType() == PurchaseDownloadItem.ActionType.DONATED)
                    .findFirst();

            if (purchasedOrDonated.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "message", "You already own this template. You can download it again.",
                        "alreadyOwned", true,
                        "templateId", template.getId()
                ));
            }

            Optional<PurchaseDownloadItem> freeDownload = existingItems.stream()
                    .filter(i -> i.getActionType() == PurchaseDownloadItem.ActionType.FREE_DOWNLOAD)
                    .findFirst();

            double amountToDeduct = switch (template.getPriceSetting()) {
                case "Paid" -> template.getPrice() != null ? template.getPrice() : 0;
                case "₱0 or donation" -> donationAmount != null ? donationAmount : 0;
                case "No Payment" -> 0;
                default -> 0;
            };

            if (user.getWallet() < amountToDeduct && amountToDeduct > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient wallet balance");
            }

            if (amountToDeduct > 0) {
                user.setWallet(user.getWallet() - amountToDeduct);
                userService.saveUser(user);
                template.setRevenue(template.getRevenue() + (float) amountToDeduct);
                templateService.saveTemplate(template);
            }

            PurchaseDownloadItem.ActionType actionType = switch (template.getPriceSetting()) {
                case "Paid" -> PurchaseDownloadItem.ActionType.PURCHASED;
                case "₱0 or donation" -> PurchaseDownloadItem.ActionType.DONATED;
                case "No Payment" -> PurchaseDownloadItem.ActionType.FREE_DOWNLOAD;
                default -> PurchaseDownloadItem.ActionType.FREE_DOWNLOAD;
            };

            if (freeDownload.isPresent() && actionType == PurchaseDownloadItem.ActionType.DONATED) {
                PurchaseDownloadItem item = freeDownload.get();
                item.setActionType(PurchaseDownloadItem.ActionType.DONATED);
                item.setAmountPaid(amountToDeduct);
                item.setActionDate(LocalDateTime.now());
                purchaseDownloadItemRepository.save(item);
            } else if (!purchasedOrDonated.isPresent()) {
                PurchaseDownloadItem item = new PurchaseDownloadItem();
                item.setUser(user);
                item.setTemplate(template);
                item.setActionType(actionType);
                item.setActionDate(LocalDateTime.now());
                item.setAmountPaid(amountToDeduct > 0 ? amountToDeduct : null);
                purchaseDownloadItemRepository.save(item);

                template.incrementDownloadCount();
                templateService.saveTemplate(template);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Purchase / donation successful",
                    "deductedAmount", amountToDeduct,
                    "templateId", template.getId()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Purchase failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/download/free")
    public ResponseEntity<Resource> downloadFreeTemplate(
            @PathVariable Long id
    ) throws IOException {
        try {
            Long userId = securityUtil.getCurrentUserId();

            Optional<Template> templateOpt = templateService.getTemplateById(id);
            Optional<User> userOpt = userService.getUserById(userId);

            if (templateOpt.isEmpty()) return ResponseEntity.notFound().build();
            if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Template template = templateOpt.get();
            User user = userOpt.get();
            List<File> files = template.getFiles();

            if (files == null || files.isEmpty()) return ResponseEntity.badRequest().build();

            List<PurchaseDownloadItem> existingItems = purchaseDownloadItemRepository.findByUserAndTemplate(user, template);

            boolean hasPaid = existingItems.stream()
                    .anyMatch(i -> i.getActionType() == PurchaseDownloadItem.ActionType.PURCHASED
                            || i.getActionType() == PurchaseDownloadItem.ActionType.DONATED);

            boolean isFree = template.getPriceSetting().equals("No Payment")
                    || template.getPriceSetting().equals("₱0 or donation");

            if (!hasPaid && !isFree) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ByteArrayResource("Payment required".getBytes()));
            }

            boolean alreadyDownloaded = existingItems.stream()
                    .anyMatch(i -> i.getActionType() == PurchaseDownloadItem.ActionType.FREE_DOWNLOAD);

            if (!hasPaid && !alreadyDownloaded) {
                PurchaseDownloadItem item = new PurchaseDownloadItem();
                item.setUser(user);
                item.setTemplate(template);
                item.setActionType(PurchaseDownloadItem.ActionType.FREE_DOWNLOAD);
                item.setActionDate(LocalDateTime.now());
                item.setAmountPaid(null);
                purchaseDownloadItemRepository.save(item);

                template.incrementDownloadCount();
                templateService.saveTemplate(template);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (File file : files) {
                    String filePath = file.getFilePath().replace("\\", "/");
                    Path absolutePath = resolveFilePath(filePath);

                    if (!Files.exists(absolutePath)) {
                        Path alternative1 = Paths.get("uploads", Paths.get(file.getFilePath()).getFileName().toString());
                        Path alternative2 = Paths.get(System.getProperty("user.dir"), "uploads", Paths.get(file.getFilePath()).getFileName().toString());
                        if (Files.exists(alternative1)) absolutePath = alternative1;
                        else if (Files.exists(alternative2)) absolutePath = alternative2;
                        else continue;
                    }

                    zos.putNextEntry(new ZipEntry(file.getFileName()));
                    Files.copy(absolutePath, zos);
                    zos.closeEntry();
                }
            }

            ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + template.getTemplateTitle().replaceAll("[^a-zA-Z0-9._-]", "_") + ".zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(("Download failed: " + e.getMessage()).getBytes()));
        }
    }

    private Path resolveFilePath(String filePath) {
        String normalizedPath = filePath.replace("\\", "/");

        if (normalizedPath.startsWith("uploads/")) {
            String projectRoot = System.getProperty("user.dir");
            return Paths.get(projectRoot, normalizedPath);
        }

        if (!normalizedPath.contains("/") && !normalizedPath.contains("\\")) {
            String projectRoot = System.getProperty("user.dir");
            return Paths.get(projectRoot, "uploads", normalizedPath);
        }

        Path path = Paths.get(normalizedPath);
        if (path.isAbsolute()) {
            return path;
        }

        String projectRoot = System.getProperty("user.dir");
        return Paths.get(projectRoot, normalizedPath);
    }

    @GetMapping("/user/library")
    public ResponseEntity<List<PurchaseDownloadItem>> getUserLibrary() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            List<PurchaseDownloadItem> items = purchaseDownloadItemRepository.findByUserOrderByActionDateDesc(userOpt.get());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/{templateId}/rate")
    public ResponseEntity<?> rateTemplate(
            @PathVariable Long templateId,
            @RequestParam Integer ratingValue
    ) {
        try {
            Long userId = securityUtil.getCurrentUserId();

            Optional<Template> templateOpt = templateService.getTemplateById(templateId);
            Optional<User> userOpt = userService.getUserById(userId);

            if (templateOpt.isEmpty()) return ResponseEntity.status(404).body("Template not found");
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("User not found");

            Template template = templateOpt.get();
            User user = userOpt.get();

            if (ratingValue < 1 || ratingValue > 5)
                return ResponseEntity.badRequest().body("Rating must be between 1 and 5");

            RatingItem ratingItem = ratingItemRepository.findByUserAndTemplate(user, template)
                    .orElseGet(RatingItem::new);

            ratingItem.setUser(user);
            ratingItem.setTemplate(template);
            ratingItem.setRatingValue(ratingValue);
            ratingItem.setRatedAt(LocalDateTime.now());

            ratingItemRepository.save(ratingItem);
            updateTemplateAverageRating(template);

            return ResponseEntity.ok(Map.of(
                    "message", "Rating saved successfully",
                    "ratingValue", ratingValue,
                    "averageRating", template.getAverageRating()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to rate template: " + e.getMessage());
        }
    }

    @GetMapping("/{templateId}/rating")
    public ResponseEntity<?> getUserRating(
            @PathVariable Long templateId
    ) {
        try {
            Long userId = securityUtil.getCurrentUserId();

            Optional<Template> templateOpt = templateService.getTemplateById(templateId);
            Optional<User> userOpt = userService.getUserById(userId);

            if (templateOpt.isEmpty() || userOpt.isEmpty()) return ResponseEntity.ok(Map.of("ratingValue", 0));

            Optional<RatingItem> ratingItem = ratingItemRepository.findByUserAndTemplate(userOpt.get(), templateOpt.get());
            int value = ratingItem.map(RatingItem::getRatingValue).orElse(0);

            return ResponseEntity.ok(Map.of("ratingValue", value));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ratingValue", 0));
        }
    }

    @GetMapping("/user/rated")
    public ResponseEntity<List<RatingItem>> getUserRatedTemplates() {
        try {
            Long userId = securityUtil.getCurrentUserId();

            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            User user = userOpt.get();

            List<Template> templates = templateService.getAllTemplates();

            List<RatingItem> ratedItems = new ArrayList<>();

            for (Template template : templates) {
                ratingItemRepository.findByUserAndTemplate(user, template)
                        .ifPresent(ratedItems::add);
            }

            ratedItems.sort((a, b) -> b.getRatedAt().compareTo(a.getRatedAt()));

            return ResponseEntity.ok(ratedItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{templateId}/rated-users")
    public ResponseEntity<List<Map<String, Object>>> getUsersWhoRatedTemplate(@PathVariable Long templateId) {
        Optional<Template> templateOpt = templateService.getTemplateById(templateId);
        if (templateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Template template = templateOpt.get();

        List<RatingItem> ratings = ratingItemRepository.findAll()
                .stream()
                .filter(r -> r.getTemplate().getId().equals(templateId))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = ratings.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userID", r.getUser().getUserID());
            map.put("userName", r.getUser().getUsername());
            map.put("ratingValue", r.getRatingValue());

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private void updateTemplateAverageRating(Template template) {
        List<RatingItem> ratings = ratingItemRepository.findAll()
                .stream()
                .filter(r -> r.getTemplate().getId().equals(template.getId()))
                .collect(Collectors.toList());

        if (ratings.isEmpty()) {
            template.setAverageRating(null); // or 0.0
        } else {
            double average = ratings.stream()
                    .mapToInt(RatingItem::getRatingValue)
                    .average()
                    .orElse(0.0);
            template.setAverageRating(average);
        }

        templateService.saveTemplate(template);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestPart("template") String templateJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            System.out.println("Received template JSON: " + templateJson);
            JsonNode rootNode = objectMapper.readTree(templateJson);

            Long templateOwner = securityUtil.getCurrentUserId();
            System.out.println("Template owner from JWT: " + templateOwner);

            ((ObjectNode) rootNode).remove("templateOwner");

            Template template = objectMapper.readValue(rootNode.toString(), Template.class);

            if (templateOwner != null) {
                template.setTemplateOwner(templateOwner);
                System.out.println("Manually set templateOwner to: " + templateOwner);
            }

            System.out.println("Template after manual set - owner: " + template.getTemplateOwner());
            System.out.println("=== END DEBUG ===");

            if (coverImage != null && !coverImage.isEmpty()) {
                String uploadsDir = "uploads/";
                Files.createDirectories(Paths.get(uploadsDir));
                String fileName = System.currentTimeMillis() + "_" +
                        coverImage.getOriginalFilename().replace(" ", "_");
                String fullFilePath = uploadsDir + fileName;
                Files.copy(coverImage.getInputStream(), Paths.get(fullFilePath));
                template.setCoverImagePath("uploads/" + fileName);
            }

            template.setFiles(new ArrayList<>());
            template.setImages(new ArrayList<>());

            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String uploadsDir = "uploads/";
                        Files.createDirectories(Paths.get(uploadsDir));
                        String fileName = System.currentTimeMillis() + "_" +
                                image.getOriginalFilename().replace(" ", "_");
                        String fullFilePath = uploadsDir + fileName;
                        Files.copy(image.getInputStream(), Paths.get(fullFilePath));

                        TemplateImage templateImage = new TemplateImage();
                        templateImage.setImagePath("uploads/" + fileName);
                        templateImage.setTemplate(template);
                        template.addImage(templateImage);
                    }
                }
            }

            if (files != null && !files.isEmpty()) {
                for (MultipartFile multipartFile : files) {
                    if (!multipartFile.isEmpty()) {
                        String uploadsDir = "uploads/";
                        Files.createDirectories(Paths.get(uploadsDir));
                        String fileName = System.currentTimeMillis() + "_" +
                                multipartFile.getOriginalFilename().replace(" ", "_");
                        String fullFilePath = uploadsDir + fileName;
                        Files.copy(multipartFile.getInputStream(), Paths.get(fullFilePath));

                        File fileEntity = new File();
                        fileEntity.setFileName(multipartFile.getOriginalFilename());
                        fileEntity.setFilePath("uploads/" + fileName);
                        fileEntity.setFileSize(multipartFile.getSize());
                        fileEntity.setFileType(multipartFile.getContentType());
                        fileEntity.setTemplate(template);

                        template.addFile(fileEntity);
                    }
                }
            }

            Template savedTemplate = templateRepository.save(template);

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

    @GetMapping("/user/my-templates")
    public ResponseEntity<List<Template>> getUserTemplates() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            List<Template> templates = templateService.getTemplatesByOwner(userId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<List<Map<String, Object>>> getTemplateFiles(@PathVariable Long id) {
        Optional<Template> templateOpt = templateService.getTemplateById(id);
        if (templateOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.emptyList());
        }

        List<File> files = templateOpt.get().getFiles();
        List<Map<String, Object>> fileDTOs = files.stream()
                .map(file -> {
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("id", file.getId());
                    fileMap.put("fileName", file.getFileName());
                    fileMap.put("filePath", file.getFilePath());
                    fileMap.put("fileType", file.getFileType());
                    fileMap.put("fileSize", file.getFileSize());
                    return fileMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileDTOs);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable Long id,
            @RequestPart("template") String templateJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "files", required = false) List<MultipartFile> files)
    {
        try {
            Long currentUserId = securityUtil.getCurrentUserId();
            Optional<Template> existingTemplateOpt = templateService.getTemplateById(id);

            if (existingTemplateOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Template not found"));
            }

            Template existingTemplate = existingTemplateOpt.get();

            if (!currentUserId.equals(existingTemplate.getTemplateOwner())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only edit your own templates"));
            }

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            System.out.println("Updating template ID: " + id);
            System.out.println("Received template JSON: " + templateJson);

            JsonNode rootNode = objectMapper.readTree(templateJson);

            // Extract filenames to delete (for images)
            List<String> filenamesToDelete = new ArrayList<>();
            if (rootNode.has("filenamesToDelete") && rootNode.get("filenamesToDelete").isArray()) {
                for (JsonNode filenameNode : rootNode.get("filenamesToDelete")) {
                    filenamesToDelete.add(filenameNode.asText());
                }
                System.out.println("Filenames to delete: " + filenamesToDelete);
            }

            // for file deletion
            List<Long> fileIdsToDelete = new ArrayList<>();
            if (rootNode.has("fileIdsToDelete") && rootNode.get("fileIdsToDelete").isArray()) {
                for (JsonNode idNode : rootNode.get("fileIdsToDelete")) {
                    fileIdsToDelete.add(idNode.asLong());
                }
                System.out.println("File IDs to delete: " + fileIdsToDelete);
            }

            // Remove custom fields before parsing to Template
            ((ObjectNode) rootNode).remove("templateOwner");
            ((ObjectNode) rootNode).remove("filenamesToDelete");
            ((ObjectNode) rootNode).remove("fileIdsToDelete");

            Template updatedTemplateData = objectMapper.readValue(rootNode.toString(), Template.class);

            if (!filenamesToDelete.isEmpty()) {
                System.out.println("=== DEBUG: STARTING DELETION ===");
                System.out.println("Filenames to delete from frontend: " + filenamesToDelete);
                System.out.println("Number of existing images: " + existingTemplate.getImages().size());

                List<TemplateImage> imagesToRemove = new ArrayList<>();

                System.out.println("\n=== EXISTING IMAGES IN DATABASE ===");
                int counter = 0;
                for (TemplateImage img : existingTemplate.getImages()) {
                    String imagePath = img.getImagePath();
                    System.out.println("Image " + counter + ":");
                    System.out.println("  Full path: '" + imagePath + "'");
                    System.out.println("  Path length: " + imagePath.length());

                    String filename = Paths.get(imagePath).getFileName().toString();

                    System.out.println("Checking: DB='" + filename + "' vs Delete='" + filenamesToDelete + "'");

                    boolean match = filenamesToDelete.contains(filename);
                    System.out.println("  Matches filenamesToDelete? " + match);

                    if (match) {
                        imagesToRemove.add(img);
                        System.out.println("  ✅ WILL DELETE THIS IMAGE");
                    }

                    System.out.println();
                    counter++;
                }

                System.out.println("=== MATCHING RESULTS ===");
                System.out.println("Total images marked for deletion: " + imagesToRemove.size());

                if (imagesToRemove.isEmpty()) {
                    System.out.println("❌ WARNING: NO IMAGES MATCHED!");
                    System.out.println("Possible issues:");
                    System.out.println("1. Database filenames don't match frontend filenames");
                    System.out.println("2. Check for spaces/encoding differences");
                    System.out.println("3. Template might not have images loaded");
                } else {
                    for (TemplateImage img : imagesToRemove) {
                        System.out.println("Removing from template: " + img.getImagePath());
                        existingTemplate.removeImage(img);
                    }
                    System.out.println("✅ Successfully removed " + imagesToRemove.size() + " images");
                }

                System.out.println("=== DEBUG: DELETION COMPLETE ===");
            }

            if (!fileIdsToDelete.isEmpty()) {
                List<File> filesToRemove = existingTemplate.getFiles().stream()
                        .filter(file -> fileIdsToDelete.contains(file.getId()))
                        .collect(Collectors.toList());

                for (File file : filesToRemove) {
                    System.out.println("Deleting file ID: " + file.getId() + " - " + file.getFileName());
                    existingTemplate.removeFile(file);
                }
            }

            existingTemplate.setTemplateTitle(updatedTemplateData.getTemplateTitle());
            existingTemplate.setTemplateDesc(updatedTemplateData.getTemplateDesc());
            existingTemplate.setPriceSetting(updatedTemplateData.getPriceSetting());
            existingTemplate.setPrice(updatedTemplateData.getPrice());
            existingTemplate.setVisibility(updatedTemplateData.getVisibility());
            existingTemplate.setEngine(updatedTemplateData.getEngine());
            existingTemplate.setType(updatedTemplateData.getType());
            existingTemplate.setUpdateDate(LocalDateTime.now());

            if (coverImage != null && !coverImage.isEmpty()) {
                String uploadsDir = "uploads/";
                Files.createDirectories(Paths.get(uploadsDir));
                String fileName = System.currentTimeMillis() + "_" +
                        coverImage.getOriginalFilename().replace(" ", "_");
                String fullFilePath = uploadsDir + fileName;
                Files.copy(coverImage.getInputStream(), Paths.get(fullFilePath));
                existingTemplate.setCoverImagePath("uploads/" + fileName);
                System.out.println("Updated cover image: " + fileName);
            }

            if (images != null && !images.isEmpty()) {
                System.out.println("Adding " + images.size() + " new images");
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String uploadsDir = "uploads/";
                        Files.createDirectories(Paths.get(uploadsDir));
                        String fileName = System.currentTimeMillis() + "_" +
                                image.getOriginalFilename().replace(" ", "_");
                        String fullFilePath = uploadsDir + fileName;
                        Files.copy(image.getInputStream(), Paths.get(fullFilePath));

                        TemplateImage templateImage = new TemplateImage();
                        templateImage.setImagePath("uploads/" + fileName);
                        templateImage.setTemplate(existingTemplate);
                        existingTemplate.addImage(templateImage);
                        System.out.println("Added new image: " + fileName);
                    }
                }
            }

            if (files != null && !files.isEmpty()) {
                System.out.println("Adding " + files.size() + " new files");
                for (MultipartFile multipartFile : files) {
                    if (!multipartFile.isEmpty()) {
                        String uploadsDir = "uploads/";
                        Files.createDirectories(Paths.get(uploadsDir));
                        String fileName = System.currentTimeMillis() + "_" +
                                multipartFile.getOriginalFilename().replace(" ", "_");
                        String fullFilePath = uploadsDir + fileName;
                        Files.copy(multipartFile.getInputStream(), Paths.get(fullFilePath));

                        File fileEntity = new File();
                        fileEntity.setFileName(multipartFile.getOriginalFilename());
                        fileEntity.setFilePath("uploads/" + fileName);
                        fileEntity.setFileSize(multipartFile.getSize());
                        fileEntity.setFileType(multipartFile.getContentType());
                        fileEntity.setTemplate(existingTemplate);

                        existingTemplate.addFile(fileEntity);
                        System.out.println("Added new file: " + fileName);
                    }
                }
            }

            Template savedTemplate = templateRepository.save(existingTemplate);
            System.out.println("Template saved successfully with ID: " + savedTemplate.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Template updated successfully!");
            response.put("templateId", savedTemplate.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update template: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}