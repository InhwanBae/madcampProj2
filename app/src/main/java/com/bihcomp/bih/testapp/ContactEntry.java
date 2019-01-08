package com.bihcomp.bih.testapp;

public class ContactEntry {
    private String name;
    private String phoneNo;
    private int photo;
    private String photoname;
    private int pvalue;

    public ContactEntry(String _name, String _pn, String _photoname, int _value, int _photo) {
        this.name = _name;
        this.phoneNo = _pn;
        this.photo = _photo;
        this.photoname = _photoname;
        this.pvalue = _value;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public int getPhotoId() {
        return photo;
    }

    public String getPhotoName() { return photoname; }

    public int getPersonValue() { return pvalue; }
}
