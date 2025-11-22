package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    boolean existsByUserIdAndTemplateId(Long userId, Long templateId);
    void deleteByUserIdAndTemplateId(Long userId, Long templateId);
    List<WishlistItem> findByUserId(Long userId);
    long countByTemplateId(Long templateId);

    Optional<WishlistItem> findByUserIdAndTemplateId(Long userId, Long templateId);
}
