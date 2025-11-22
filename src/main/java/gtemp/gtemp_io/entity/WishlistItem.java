package gtemp.gtemp_io.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "template_id"})
})
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "added_date", nullable = false)
    private LocalDateTime addedDate;

    public WishlistItem() {
        this.addedDate = LocalDateTime.now();
    }

    public WishlistItem(Long userId, Long templateId) {
        this.userId = userId;
        this.templateId = templateId;
        this.addedDate = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    @Override
    public String toString() {
        return "WishlistItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", templateId=" + templateId +
                ", addedDate=" + addedDate +
                '}';
    }
}
