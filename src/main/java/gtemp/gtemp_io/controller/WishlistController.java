package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.entity.WishlistItem;
import gtemp.gtemp_io.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:5174")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishlistItem>> getUserWishlist(@PathVariable Long userId) {
        List<WishlistItem> wishlist = wishlistService.getUserWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping
    public ResponseEntity<WishlistItem> addToWishlist(@RequestBody WishlistRequest request) {
        WishlistItem item = wishlistService.addToWishlist(request.getUserId(), request.getTemplateId());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/user/{userId}/template/{templateId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long userId, @PathVariable Long templateId) {
        wishlistService.removeFromWishlist(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/template/{templateId}")
    public ResponseEntity<Boolean> isInWishlist(@PathVariable Long userId, @PathVariable Long templateId) {
        boolean exists = wishlistService.isInWishlist(userId, templateId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/template/{templateId}/count")
    public ResponseEntity<Long> getWishlistCount(@PathVariable Long templateId) {
        long count = wishlistService.getWishlistCount(templateId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggleWishlist(@RequestBody WishlistRequest request) {
        boolean isNowInWishlist = wishlistService.toggleWishlist(request.getUserId(), request.getTemplateId());
        return ResponseEntity.ok(isNowInWishlist);
    }

    public static class WishlistRequest {
        private Long userId;
        private Long templateId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
    }
}
