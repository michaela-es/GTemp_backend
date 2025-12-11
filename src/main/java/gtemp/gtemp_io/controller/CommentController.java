package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.entity.Comment;
import gtemp.gtemp_io.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")  // Change to * for easier testing
public class CommentController {

    @Autowired
    private CommentService commentService;

    // Option 1: Keep your current endpoint (query parameter)
    @GetMapping
    public List<Comment> getAllComments(@RequestParam Long templateID) {
        return commentService.getCommentsByTemplate(templateID);
    }

    @GetMapping("/template/{templateId}")
    public List<Comment> getCommentsByTemplateId(@PathVariable Long templateId) {
        return commentService.getCommentsByTemplate(templateId);
    }

    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.createComment(comment);
    }

    @GetMapping("/test")
    public String test() {
        return "CommentController is working!";
    }
}