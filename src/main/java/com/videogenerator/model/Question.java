package com.videogenerator.model;

import java.util.List;

public class Question {
    private String text;
    private String image;
    private List<String> options;
    private int correctIndex;

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }
}
