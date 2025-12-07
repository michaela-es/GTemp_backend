package gtemp.gtemp_io.dto;


public class UpdateUserRequest {

    private long userID;
    private String username;
    private String email;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(long userID, String username, String email) {
        this.userID = userID;
        this.username = username;
        this.email = email;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "userID=" + userID +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
