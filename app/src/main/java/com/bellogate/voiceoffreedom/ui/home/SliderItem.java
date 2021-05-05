package com.bellogate.voiceoffreedom.ui.home;


public class SliderItem {

    private String description;
    private int image;

    public SliderItem(String description, int image) {
        this.description = description;
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
