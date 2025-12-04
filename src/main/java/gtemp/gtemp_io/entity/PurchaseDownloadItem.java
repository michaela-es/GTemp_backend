package gtemp.gtemp_io.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_download_items")
public class PurchaseDownloadItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    // Enum for action type
    public enum ActionType {
        PURCHASED,
        DONATED,
        FREE_DOWNLOAD
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = true)
    private Double amountPaid; // For purchases/donations only

    public PurchaseDownloadItem() {}

    public PurchaseDownloadItem(User user, Template template, LocalDateTime actionDate, ActionType actionType, Double amountPaid) {
        this.user = user;
        this.template = template;
        this.actionDate = actionDate;
        this.actionType = actionType;
        this.amountPaid = amountPaid;
    }

    // Getters and setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

}
