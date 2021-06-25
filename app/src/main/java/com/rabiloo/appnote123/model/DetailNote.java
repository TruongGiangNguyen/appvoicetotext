package com.rabiloo.appnote123.model;

public class DetailNote {
    String idDetailNote;
    String idNote;
    String dateFeature;
    String note;
    String time;
    String durationRecord;
    String downloadUri;

    public DetailNote() {
    }

    public DetailNote(String idDetailNote, String idNote, String dateFeature, String note, String time, String durationRecord, String downloadUri) {
        this.idDetailNote = idDetailNote;
        this.idNote = idNote;
        this.dateFeature = dateFeature;
        this.note = note;
        this.time = time;
        this.durationRecord = durationRecord;
        this.downloadUri = downloadUri;
    }

    public String getIdDetailNote() {
        return idDetailNote;
    }

    public void setIdDetailNote(String idDetailNote) {
        this.idDetailNote = idDetailNote;
    }

    public String getIdNote() {
        return idNote;
    }

    public void setIdNote(String idNote) {
        this.idNote = idNote;
    }

    public String getDateFeature() {
        return dateFeature;
    }

    public void setDateFeature(String dateFeature) {
        this.dateFeature = dateFeature;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDurationRecord() {
        return durationRecord;
    }

    public void setDurationRecord(String durationRecord) {
        this.durationRecord = durationRecord;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }
}
