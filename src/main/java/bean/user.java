package bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class user {

    @JsonProperty("Username")
    private String Username;

    @JsonProperty("Password")
    private String Password;

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
