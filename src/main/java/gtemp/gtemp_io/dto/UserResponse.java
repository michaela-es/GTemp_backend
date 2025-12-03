package gtemp.gtemp_io.dto;

public class UserResponse {
    private String username;
    private String email;
    private Double wallet;
    private Long userID;
    public UserResponse(String username, String email, Double wallet, Long userID) {
        this.username = username;
        this.email = email;
        this.wallet = wallet;
        this.userID = userID;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Double getWallet() { return wallet; }
    public Long getUserID() {
        return userID;
    }

}
