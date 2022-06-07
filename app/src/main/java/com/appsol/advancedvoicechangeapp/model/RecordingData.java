package com.appsol.advancedvoicechangeapp.model;

import android.net.Uri;

public class RecordingData {

    public long file_duration;


    public Uri getFileIri() {
        return fileIri;
    }

    public void setFileIri(Uri fileIri) {
        this.fileIri = fileIri;
    }

    public Uri fileIri;

    public RecordingData(long file_duration, String file_displayName, Long file_date, long file_Size, Uri fileIri) {
        this.file_duration = file_duration;
        this.file_displayName = file_displayName;
        this.file_date = file_date;
        this.file_Size = file_Size;
        this.fileIri = fileIri;
    }

    public long getFile_duration() {
        return file_duration;
    }

    public void setFile_duration(long file_duration) {
        this.file_duration = file_duration;
    }

    public String getFile_displayName() {
        return file_displayName;
    }

    public void setFile_displayName(String file_displayName) {
        this.file_displayName = file_displayName;
    }

    public Long getFile_date() {
        return file_date;
    }

    public void setFile_date(Long file_date) {
        this.file_date = file_date;
    }

    public long getFile_Size() {
        return file_Size;
    }

    public void setFile_Size(long file_Size) {
        this.file_Size = file_Size;
    }

    public String file_displayName;

    public Long file_date;

    public long file_Size;


}
