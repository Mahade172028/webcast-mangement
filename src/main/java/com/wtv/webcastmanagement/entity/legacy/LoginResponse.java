package com.wtv.webcastmanagement.entity.legacy;

import java.time.LocalDateTime;

public class LoginResponse {
    public String access_token;
    public LocalDateTime expirationDate;
    public String username;
    public String name;
    public String email;
    public int userId;
}
