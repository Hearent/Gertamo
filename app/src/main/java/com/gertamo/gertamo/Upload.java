package com.gertamo.gertamo;

public class Upload {
    private String imageUri;
    private int like;
    private int desclike;
    private String author;
    private String id;
    private String Uid;
    private String name;
    private long date;
    private long descdate;

    public Upload() {
    }



    public Upload(String imageuri, int Like, int DESClike, String UploadId, String UserID, String Author, String Name, long Date, long DESCDate) {
        imageUri = imageuri;
        like = Like;
        desclike = DESClike;
        id = UploadId;
        Uid = UserID;
        author = Author;
        name = Name;
        date = Date;
        descdate = DESCDate;
    }

    public Upload(String imageuri, int Like, String Author, String Name, long Date) {
        imageUri = imageuri;
        like = Like;
        author = Author;
        name = Name;
        date = Date;
    }

    public Upload(String Author) {
        author = Author;
    }

    public Upload(int Like) {
        like = Like;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public int getDesclike() {
        return desclike;
    }

    public void setDesclike(int desclike) {
        this.desclike = desclike;
    }
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDescdate() {
        return descdate;
    }

    public void setDescdate(long descdate) {
        this.descdate = descdate;
    }
}
