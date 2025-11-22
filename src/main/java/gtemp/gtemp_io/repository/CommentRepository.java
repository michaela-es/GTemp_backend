package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTemplateID(Long templateID);

    List<Comment> findByParentId(Long parentId);

    List<Comment> findByAuthor(String author);

}