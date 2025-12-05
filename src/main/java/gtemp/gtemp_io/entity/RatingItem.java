package gtemp.gtemp_io.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rating_items")
public class RatingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private Template template;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "rating_value")
    private Integer ratingValue; // 1-5

    @Column(name = "rated_at")
    private LocalDateTime ratedAt = LocalDateTime.now();

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getRatingValue() { return ratingValue; }
    public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }

    public LocalDateTime getRatedAt() { return ratedAt; }
    public void setRatedAt(LocalDateTime ratedAt) { this.ratedAt = ratedAt; }
}
