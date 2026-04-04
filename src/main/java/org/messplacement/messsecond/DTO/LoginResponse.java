package org.messplacement.messsecond.DTO;

public class LoginResponse {
    private String token;
    private String role;
    private String regNo;

    public LoginResponse() {}

    public LoginResponse(String token, String role, String regNo) {
        this.token = token;
        this.role = role;
        this.regNo = regNo;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
}
