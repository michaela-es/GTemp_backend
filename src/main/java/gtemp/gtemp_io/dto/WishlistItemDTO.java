package gtemp.gtemp_io.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WishlistItemDTO {

    private Long templateId;
    private String title;
    private String coverImagePath;
}
