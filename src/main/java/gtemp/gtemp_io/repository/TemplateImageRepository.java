package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.TemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateImageRepository extends JpaRepository<TemplateImage, Long> {
    List<TemplateImage> findByTemplateId(Long templateId);
}
