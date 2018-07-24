package com.example.anmol.stickynote;

public class data {

    String notes,color,imageurl,font,imageuri,rdate,rtime;

    public data(){

    }

    public data(String notes, String color, String imageurl, String font, String imageuri, String rdate, String rtime) {
        this.notes = notes;
        this.color = color;
        this.imageurl = imageurl;
        this.font = font;
        this.imageuri = imageuri;
        this.rdate = rdate;
        this.rtime = rtime;
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
