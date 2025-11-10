package gtemp.gtemp_io.entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Template")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateID;

    @Column(name = "template_title", nullable = false)
    private String templateTitle;

    @Column(name = "price")
    private float price;

    @Column(name = "template_desc", length = 1000)
    private String templateDesc;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "views")
    private int views;

    @Column(name = "rating")
    private float rating;

    @Column(name = "average_rating")
    private float averageRating;

    @Column(name = "downloads")
    private int downloads;

    @Column(name = "visibility")
    private boolean visibility;

    @Column(name = "wishlist_count")
    private int wishlistCount;

    @Column(name = "is_wishlisted")
    private boolean isWishlisted;

    @Column(name = "revenue")
    private float revenue;

    @Column(name = "engine")
    private String engine;

    @Column(name = "type")
    private String type;

    @Column(name = "template_image_path")
    private String templateImagePath;

    public Template() {
    }

    public Template(String templateTitle, float price, String templateDesc, LocalDateTime releaseDate,
                    LocalDateTime updateDate, int views, float rating, float averageRating, int downloads,
                    boolean visibility, int wishlistCount, boolean isWishlisted, float revenue,
                    String engine, String type, String templateImagePath) {
        this.templateTitle = templateTitle;
        this.price = price;
        this.templateDesc = templateDesc;
        this.releaseDate = releaseDate;
        this.updateDate = updateDate;
        this.views = views;
        this.rating = rating;
        this.averageRating = averageRating;
        this.downloads = downloads;
        this.visibility = visibility;
        this.wishlistCount = wishlistCount;
        this.isWishlisted = isWishlisted;
        this.revenue = revenue;
        this.engine = engine;
        this.type = type;
        this.templateImagePath = templateImagePath;
    }

    public Long getId() {
        return templateID;
    }

    public void setTemplateID(Long templateID) {
        this.templateID = templateID;
    }

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void setTemplateTitle(String templateTitle) {
        this.templateTitle = templateTitle;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public int getWishlistCount() {
        return wishlistCount;
    }

    public void addFile(File file) {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        this.files.add(file);
        file.setTemplate(this);
    }

    public void setWishlistCount(int wishlistCount) {
        this.wishlistCount = wishlistCount;
    }

    public boolean isWishlisted() {
        return isWishlisted;
    }

    public void setWishlisted(boolean isWishlisted) {
        this.isWishlisted = isWishlisted;
    }

    public float getRevenue() {
        return revenue;
    }

    public void setRevenue(float revenue) {
        this.revenue = revenue;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemplateImagePath() {
        return templateImagePath;
    }

    public void setTemplateImagePath(String templateImagePath) {
        this.templateImagePath = templateImagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template)) return false;
        Template template = (Template) o;
        return Objects.equals(templateID, template.templateID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateID);
    }

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<File> files = new ArrayList<>();

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}