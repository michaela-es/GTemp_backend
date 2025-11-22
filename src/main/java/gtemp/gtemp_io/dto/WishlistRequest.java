package gtemp.gtemp_io.dto;

public class WishlistRequest {
    private Long userId;
    private Long templateId;

    public WishlistRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
}