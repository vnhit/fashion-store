package com.fashionstore.models;

public class Category {
    private int id;
    private String name;
    private String nameVn;
    private String iconPath;
    
    public Category() {}
    
    public Category(int id, String name, String nameVn) {
        this.id = id;
        this.name = name;
        this.nameVn = nameVn;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getNameVn() { return nameVn; }
    public void setNameVn(String nameVn) { this.nameVn = nameVn; }
    
    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
}




























