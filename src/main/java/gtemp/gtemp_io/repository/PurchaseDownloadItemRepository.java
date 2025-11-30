package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.PurchaseDownloadItem;
import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseDownloadItemRepository extends JpaRepository<PurchaseDownloadItem, Long> {

    // Add this method:
    List<PurchaseDownloadItem> findByUserAndTemplate(User user, Template template);

    // Optional: existing method to get all items for a user
    List<PurchaseDownloadItem> findByUserOrderByActionDateDesc(User user);
}
