package com.example.chatapp;

public class Users
{
    private String name;
    private String status;
    private String image;
    private String thumb_nail;

    public Users() {
    }

    public Users(String name, String status, String image , String thumb_nail) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_nail = thumb_nail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setThumb_nail(String thumb_nail) {
        this.thumb_nail = thumb_nail;
    }

    public String getThumb_nail() {
        return thumb_nail;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }
}
