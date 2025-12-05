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

    @Column(name = "category")
    private String category;

    @Column(name = "genre")
    private String genre;

    @Column(name = "template_owner")
    private Long templateOwner;

    @Column(name = "template_img")
    private String templateImg;

    @Column(name = "template_rating")
    private Double templateRating;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "cover_image_path")
    private String coverImagePath;

    @Column(name = "views")
    private Integer views = 0;

    @Column(name = "downloads")
    private Integer downloads = 0;

    @Column(name = "rating")
    private Float rating = 0.0f;

    @Column(name = "average_rating")
    private Double averageRating;


    @Column(name = "wishlist_count")
    private Integer wishlistCount = 0;

    @Column(name = "is_wishlisted")
    private Boolean isWishlisted = false;

    @Column(name = "revenue")
    private Float revenue = 0.0f;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TemplateImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<File> files = new ArrayList<>();

    @Column(name = "price_setting")
    private String priceSetting; // "₱0 or donation", "Paid", "No Payment"

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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Long getTemplateOwner() { return templateOwner; }
    public void setTemplateOwner(Long templateOwner) { this.templateOwner = templateOwner; }

    public String getTemplateImg() { return templateImg; }
    public void setTemplateImg(String templateImg) { this.templateImg = templateImg; }

    public Double getTemplateRating() { return templateRating; }
    public void setTemplateRating(Double templateRating) { this.templateRating = templateRating; }

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

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }


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

    public String getPriceSetting() { return priceSetting; }
    public void setPriceSetting(String priceSetting) { this.priceSetting = priceSetting; }

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