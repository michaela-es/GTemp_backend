package gtemp.gtemp_io.dto;


public class FileDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;

    public FileDTO(Long id, String fileName, String filePath, String fileType, Long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
}