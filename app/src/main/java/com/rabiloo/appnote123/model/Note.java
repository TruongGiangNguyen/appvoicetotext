package com.rabiloo.appnote123.model;

public class Note {
    String idNote;
    String date;
    String timeCreate;
    int coutNote;
    String email;

    public Note() {
    }

    public Note(String idNote, String date, String timeCreate, int coutNote, String email) {
        this.idNote = idNote;
        this.date = date;
        this.timeCreate = timeCreate;
        this.coutNote = coutNote;
        this.email = email;
    }

    public String getIdNote() {
        return idNote;
    }

    public void setIdNote(String idNote) {
        this.idNote = idNote;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(String timeCreate) {
        this.timeCreate = timeCreate;
    }

    public int getCoutNote() {
        return coutNote;
    }

    public void setCoutNote(int coutNote) {
        this.coutNote = coutNote;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}