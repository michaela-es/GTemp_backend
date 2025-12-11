package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.entity.WishlistItem;
import gtemp.gtemp_io.service.WishlistService;
import gtemp.gtemp_io.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/my-wishlist")
    public ResponseEntity<List<WishlistItem>> getMyWishlist() {
        Long userId = securityUtil.getCurrentUserId();
        List<WishlistItem> wishlist = wishlistService.getUserWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping
    public ResponseEntity<WishlistItem> addToWishlist(@RequestBody Map<String, Long> request) {
        Long userId = securityUtil.getCurrentUserId();
        Long templateId = request.get("templateId");
        WishlistItem item = wishlistService.addToWishlist(userId, templateId);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/template/{templateId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long templateId) {
        Long userId = securityUtil.getCurrentUserId();
        wishlistService.removeFromWishlist(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggleWishlist(@RequestBody Map<String, Long> request) {
        Long userId = securityUtil.getCurrentUserId();
        Long templateId = request.get("templateId");
        boolean isNowInWishlist = wishlistService.toggleWishlist(userId, templateId);
        return ResponseEntity.ok(isNowInWishlist);
    }

    @GetMapping("/template/{templateId}/status")
    public ResponseEntity<Boolean> isInWishlist(@PathVariable Long templateId) {
        Long userId = securityUtil.getCurrentUserId();
        boolean exists = wishlistService.isInWishlist(userId, templateId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/template/{templateId}/count")
    public ResponseEntity<Long> getWishlistCount(@PathVariable Long templateId) {
        long count = wishlistService.getWishlistCount(templateId);
        return ResponseEntity.ok(count);
    }
}
