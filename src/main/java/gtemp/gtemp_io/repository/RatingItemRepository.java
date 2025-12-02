package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.RatingItem;
import gtemp.gtemp_io.entity.Template;
import gtemp.gtemp_io.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RatingItemRepository extends JpaRepository<RatingItem, Long> {
    Optional<RatingItem> findByUserAndTemplate(User user, Template template);
}
