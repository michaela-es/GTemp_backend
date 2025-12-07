package gtemp.gtemp_io.dto;

public class TemplateHomePageDTO {

    private Long id;
    private String templateTitle;
    private String templateDesc;
    private String coverImagePath;
    private Double averageRating;
    private Float price;
    private Integer downloadCount;
    private String engine;
    private String type;

    public TemplateHomePageDTO(Long id, String templateTitle, String templateDesc, String coverImagePath,
                               Double averageRating, Float price, Integer downloadCount, String engine, String type) {
        this.id = id;
        this.templateTitle = templateTitle;
        this.templateDesc = templateDesc;
        this.coverImagePath = coverImagePath;
        this.averageRating = averageRating;
        this.price = price;
        this.downloadCount = downloadCount;
        this.engine = engine;
        this.type = type;
    }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTemplateDesc(String templateDesc){
        this.templateDesc = templateDesc;
    }

    public String getTemplateDesc(){
        return templateDesc;
    }

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void setTemplateTitle(String templateTitle) {
        this.templateTitle = templateTitle;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
}
