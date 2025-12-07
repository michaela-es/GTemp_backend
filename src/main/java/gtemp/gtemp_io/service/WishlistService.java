//WishlistService.java
package gtemp.gtemp_io.service;

import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.WishlistItem;
import gtemp.gtemp_io.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import gtemp.gtemp_io.repository.TemplateRepository;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private TemplateRepository templateRepository;
    
    public boolean isInWishlist(Long userId, Long templateId) {
        return wishlistRepository.existsByUserIdAndTemplateId(userId, templateId);
    }

    public WishlistItem addToWishlist(Long userId, Long templateId) {
        if (isInWishlist(userId, templateId)) {
            throw new RuntimeException("Template already in wishlist");
        }

        WishlistItem item = new WishlistItem(userId, templateId);
        return wishlistRepository.save(item);
    }

    public void removeFromWishlist(Long userId, Long templateId) {
        WishlistItem item = wishlistRepository.findByUserIdAndTemplateId(userId, templateId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));
        wishlistRepository.delete(item);
    }

    public boolean toggleWishlist(Long userId, Long templateId) {
        Optional<WishlistItem> itemOpt = wishlistRepository.findByUserIdAndTemplateId(userId, templateId);
        boolean added;

        if (itemOpt.isPresent()) {
            wishlistRepository.delete(itemOpt.get());
            added = false;
        } else {
            WishlistItem item = new WishlistItem(userId, templateId);
            wishlistRepository.save(item);
            added = true;
        }

        // ✅ Update the wishlist count on the template
        updateTemplateWishlistCount(templateId);

        return added;
    }




    public List<WishlistItem> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public long getWishlistCount(Long templateId) {
        return wishlistRepository.countByTemplateId(templateId);
    }

    public void updateTemplateWishlistCount(Long templateId) {
        long count = wishlistRepository.countByTemplateId(templateId);
        templateRepository.findById(templateId).ifPresent(template -> {
            template.setWishlistCount((int) count);
            templateRepository.save(template);
        });
    }

}
