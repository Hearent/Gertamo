package com.gertamo.gertamo;

public class User {
    private String email;
    private String sex;
    private String year;
    private int ban;
    private int perm;

    public User() {
    }




    public User(String Email, String Sex, String Year, int Ban, int Perm) {
        email = Email;
        sex = Sex;
        year = Year;
        ban = Ban;
        perm = Perm;
    }

    public User(int Ban){
        ban = Ban;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public int getBan() {
        return ban;
    }

    public void setBan(int ban) {
        this.ban = ban;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
