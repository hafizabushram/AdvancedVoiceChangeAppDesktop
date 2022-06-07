package com.appsol.advancedvoicechangeapp.model;

public class MusicData {


    public long track_duration;

    public String track_displayName;

    public String track_data;

    public long track_Id;

    public String track_Title;

    public long track_Size;

    public String getTrack_data() {
        return this.track_data;
    }

    public long getTrack_Size() {
        return track_Size;
    }

    public void setTrack_Size(long track_Size) {
        this.track_Size = track_Size;
    }

    public long getTrack_Id() {
        return this.track_Id;
    }

    public void setTrack_Id(long track_Id) {
        this.track_Id = track_Id;
    }

    public long getTrack_duration() {
        return this.track_duration;
    }

    public void setTrack_duration(long track_duration) {
        this.track_duration = track_duration;
    }

    public String getTrack_Title() {
        return this.track_Title;
    }

    public void setTrack_Title(String track_Title) {
        this.track_Title = track_Title;
    }

    public void setTrack_data(String track_data) {
        this.track_data = track_data;
    }

    public String getTrack_displayName() {
        return this.track_displayName;
    }

    public void setTrack_displayName(String track_displayName) {
        this.track_displayName = track_displayName;
    }
}
