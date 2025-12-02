package gtemp.gtemp_io.dto;

public class UserResponse {
    private String username;
    private String email;
    private Double wallet;

    public UserResponse(String username, String email, Double wallet) {
        this.username = username;
        this.email = email;
        this.wallet = wallet;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Double getWallet() { return wallet; }
}
