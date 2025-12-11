package gtemp.gtemp_io.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
}