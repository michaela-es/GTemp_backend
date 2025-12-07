//Template.java
package gtemp.gtemp_io.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "templateid")
    private Long id;

    @Column(name = "template_title")
    private String templateTitle;

    @Column(name = "template_desc")
    private String templateDesc;

    @Column(name = "price")
    private Float price;

    @Column(name = "visibility")
    private Boolean visibility;

    @Column(name = "engine")
    private String engine;

    @Column(name = "type")
    private String type;

    @JsonProperty
    @Column(name = "template_owner", nullable = false)
    private Long templateOwner;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "cover_image_path")
    private String coverImagePath;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "revenue")
    private Float revenue = 0.0f;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TemplateImage> images = new ArrayList<>();

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<File> files = new ArrayList<>();

    @Column(name = "price_setting")
    private String priceSetting; // "₱0 or donation", "Paid", "No Payment"

    @Transient
    private String templateOwnerUsername;

    @Column(name = "wishlist_count")
    private Integer wishlistCount = 0;
    
    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @OneToMany
    @JoinColumn(name = "template_id", referencedColumnName = "templateid")
    private List<Comment> comments = new ArrayList<>();

    public Template() {
        this.releaseDate = LocalDateTime.now();
    }

    public String getTemplateOwnerUsername() {
        return templateOwnerUsername;
    }

    public void setTemplateOwner(String templateOwnerUsername) {
        this.templateOwnerUsername = this.templateOwnerUsername;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTemplateTitle() { return templateTitle; }
    public void setTemplateTitle(String templateTitle) { this.templateTitle = templateTitle; }

    public String getTemplateDesc() { return templateDesc; }
    public void setTemplateDesc(String templateDesc) { this.templateDesc = templateDesc; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public Boolean getVisibility() { return visibility; }
    public void setVisibility(Boolean visibility) { this.visibility = visibility; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getTemplateOwner() { return templateOwner; }
    public void setTemplateOwner(Long templateOwner) { this.templateOwner = templateOwner; }

    public LocalDateTime getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }

    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Float getRevenue() { return revenue; }
    public void setRevenue(Float revenue) { this.revenue = revenue; }

    public List<TemplateImage> getImages() { return images; }
    public void setImages(List<TemplateImage> images) { this.images = images; }

    public List<File> getFiles() { return files; }
    public void setFiles(List<File> files) { this.files = files; }

    public String getPriceSetting() { return priceSetting; }
    public void setPriceSetting(String priceSetting) { this.priceSetting = priceSetting; }

    public Integer getWishlistCount() {
        return wishlistCount;
    }

    public void setWishlistCount(Integer wishlistCount) {
        this.wishlistCount = wishlistCount;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void addImage(TemplateImage image) {
        images.add(image);
        image.setTemplate(this);
    }

    public void removeImage(TemplateImage image) {
        images.remove(image);
        image.setTemplate(null);
    }

    public void addFile(File file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(file);
        file.setTemplate(this);
    }

    public void removeFile(File file) {
        files.remove(file);
        file.setTemplate(null);
    }

    public void setTemplateOwnerUsername(String templateOwnerUsername) {
        this.templateOwnerUsername = templateOwnerUsername;
    }

    public void incrementDownloadCount() {
        if (this.downloadCount == null) this.downloadCount = 0;
        this.downloadCount += 1;
    }

}