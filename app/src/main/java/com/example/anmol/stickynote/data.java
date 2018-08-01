package com.example.anmol.stickynote;

public class data {

    String notes,color,imageurl,font,imageuri,rdate,rtime,category,starred;

    public data(){

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStarred() {
        return starred;
    }

    public void setStarred(String starred) {
        this.starred = starred;
    }

    public data(String notes, String color, String imageurl, String starred, String font, String imageuri, String rdate, String rtime, String category) {
        this.notes = notes;
        this.color = color;
        this.imageurl = imageurl;
        this.category=category;

        this.font = font;
        this.imageuri = imageuri;
        this.rdate = rdate;
        this.rtime = rtime;
        this.starred=starred;
    }

    public String getNotes() {

        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getImageuri() {
        return imageuri;
    }

    public void setImageuri(String imageuri) {
        this.imageuri = imageuri;
    }

    public String getRdate() {
        return rdate;
    }

    public void setRdate(String rdate) {
        this.rdate = rdate;
    }

    public String getRtime() {
        return rtime;
    }

    public void setRtime(String rtime) {
        this.rtime = rtime;
    }
}
