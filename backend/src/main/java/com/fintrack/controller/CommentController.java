package com.fintrack.controller;

import com.fintrack.audit.AuditService;
import com.fintrack.dto.CommentRequest;
import com.fintrack.entity.Comment;
import com.fintrack.exception.NotFoundException;
import com.fintrack.repository.CommentRepository;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.InputGuard;
import com.fintrack.service.ProfileService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentRepository comments;
    private final ProfileService profiles;
    private final AuditService audit;

    public CommentController(CommentRepository comments, ProfileService profiles, AuditService audit) {
        this.comments = comments;
        this.profiles = profiles;
        this.audit = audit;
    }

    @GetMapping("/persons/{personId}/comments")
    public List<Comment> list(@PathVariable Long personId) {
        return comments.findByPersonIdOrderByCreatedAtAsc(personId);
    }

    @PostMapping("/persons/{personId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(@PathVariable Long personId, @Valid @RequestBody CommentRequest request) {
        profiles.person(personId);
        Comment comment = new Comment();
        comment.personId = personId;
        comment.author = AuditService.currentUser();
        comment.body = InputGuard.check(request.body());
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = comment.createdAt;
        comments.save(comment);
        audit.log("COMENTARIO_CREADO", "Comentario " + comment.id + " en persona " + personId);
        return comment;
    }

    @PutMapping("/comments/{id}")
    public Comment update(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        Comment comment = find(id);
        CurrentUser.requireAccess(comment.personId);
        comment.body = InputGuard.check(request.body());
        comment.updatedAt = LocalDateTime.now();
        comments.save(comment);
        audit.log("COMENTARIO_EDITADO", "Comentario " + id);
        return comment;
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Comment comment = find(id);
        CurrentUser.requireAccess(comment.personId);
        comments.delete(comment);
        audit.log("COMENTARIO_ELIMINADO", "Comentario " + id);
    }

    private Comment find(Long id) {
        return comments.findById(id).orElseThrow(() -> new NotFoundException("Comentario no encontrado."));
    }
}
