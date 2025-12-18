package com.fashionstore.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String avatarPath;
    private String membershipLevel;
    private int points;
    private String dateOfBirth;
    private String gender;
    
    public User() {}
    
    public User(int id, String name, String email, String phone, String membershipLevel, int points) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipLevel = membershipLevel;
        this.points = points;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    
    public String getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(String membershipLevel) { this.membershipLevel = membershipLevel; }
    
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}




