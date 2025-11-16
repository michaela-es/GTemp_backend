package gtemp.gtemp_io.entity;

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
    @JoinColumn(name = "templateid")
    private Long id;

    private String templateTitle;
    private String templateDesc;
    private Float price;
    private Boolean visibility;
    private String engine;
    private String type;

    private LocalDateTime releaseDate;
    private LocalDateTime updateDate;
    private String coverImagePath;

    private Integer views = 0;
    private Integer downloads = 0;
    private Float rating = 0.0f;

    // ADDED: Missing fields from your JSON
    private Float averageRating = 0.0f;
    private Integer wishlistCount = 0;
    private Boolean isWishlisted = false;
    private Float revenue = 0.0f;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TemplateImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<File> files = new ArrayList<>();

    public Template() {}

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

    public LocalDateTime getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }

    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public Integer getDownloads() { return downloads; }
    public void setDownloads(Integer downloads) { this.downloads = downloads; }

    public Float getRating() { return rating; }
    public void setRating(Float rating) { this.rating = rating; }

    // ADDED: Getters and setters for new fields
    public Float getAverageRating() { return averageRating; }
    public void setAverageRating(Float averageRating) { this.averageRating = averageRating; }

    public Integer getWishlistCount() { return wishlistCount; }
    public void setWishlistCount(Integer wishlistCount) { this.wishlistCount = wishlistCount; }

    public Boolean getIsWishlisted() { return isWishlisted; }
    public void setIsWishlisted(Boolean isWishlisted) { this.isWishlisted = isWishlisted; }

    public Float getRevenue() { return revenue; }
    public void setRevenue(Float revenue) { this.revenue = revenue; }

    public List<TemplateImage> getImages() { return images; }
    public void setImages(List<TemplateImage> images) { this.images = images; }

    public List<File> getFiles() { return files; }
    public void setFiles(List<File> files) { this.files = files; }

    // Helper methods
    public void addImage(TemplateImage image) {
        images.add(image);
        image.setTemplate(this);
    }

    public void removeImage(TemplateImage image) {
        images.remove(image);
        image.setTemplate(null);
    }

    public void addFile(File file) {
        files.add(file);
        file.setTemplate(this);
    }

    public void removeFile(File file) {
        files.remove(file);
        file.setTemplate(null);
    }
}