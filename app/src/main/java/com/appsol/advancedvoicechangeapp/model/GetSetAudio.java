
package com.appsol.advancedvoicechangeapp.model;

public class GetSetAudio {

    private int imageId;
    private int imageId2;

    private int freq;
    private String title;
    boolean isPlaing = false;

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    private float pitch;
    private int tempo;

    public GetSetAudio(int imageId, String title, int imageId2, int freq, boolean isPlaing) {
        this.imageId = imageId;
        this.imageId2 = imageId2;
        this.isPlaing = isPlaing;
        this.title = title;
        this.freq = freq;

    }

    public GetSetAudio(int imageId2, String title, float pitch, int tempo) {
        this.imageId2 = imageId2;
        this.title = title;
        this.pitch = pitch;
        this.tempo = tempo;
    }

    public int getFreq() {
        return freq;
    }

    public float getPitch() {
        return pitch;
    }

    public int getTempo() {
        return tempo;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public boolean getisPlaing() {
        return isPlaing;
    }

    public void setIsPlaing(boolean isPlaing) {
        this.isPlaing = isPlaing;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageId2() {
        return imageId2;
    }


}