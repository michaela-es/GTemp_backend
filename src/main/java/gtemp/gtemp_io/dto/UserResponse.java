package gtemp.gtemp_io.dto;

import gtemp.gtemp_io.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long userID;
    private String username;
    private String email;
    private Double wallet;

    public UserResponse(User user) {
        this.userID = user.getUserID();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.wallet = user.getWallet();
    }
}