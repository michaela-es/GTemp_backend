package gtemp.gtemp_io.service;

import  gtemp.gtemp_io.entity.Comment;
import gtemp.gtemp_io.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByTemplate(Long templateID) {
        return commentRepository.findByTemplateID(templateID);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment createReply(Long parentId, Comment reply) {
        if (!commentRepository.existsById(parentId)) {
            throw new RuntimeException("Parent comment not found");
        }
        reply.setParentId(parentId);
        return commentRepository.save(reply);
    }

    public boolean commentExists(Long id) {
        return commentRepository.existsById(id);
    }
}