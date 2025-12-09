package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByVisibilityTrue();
    List<Template> findByTemplateOwnerAndVisibilityTrue(Long templateOwner);
    List<Template> findByTemplateOwner(Long templateOwner);
}