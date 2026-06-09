package com.videogenerator.model;

import java.util.List;

public class Question {
    private String type;
    private String text;
    private String image;
    private List<String> options;
    private Integer correctIndex;
    private String correctImage;
    private String incorrectImage;
    private Integer repetitions;
    private String silhouetteImage;
    private String revealImage;

    public String getType() {
        return type == null ? "quiz" : type;
    }

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean hasCorrectAnswer() {
        return correctIndex != null;
    }

    public int getCorrectIndex() {
        return correctIndex == null ? -1 : correctIndex;
    }

    public String getCorrectImage() {
        return correctImage;
    }

    public String getIncorrectImage() {
        return incorrectImage;
    }

    public int getRepetitions() {
        return repetitions == null ? 6 : repetitions;
    }

    public String getSilhouetteImage() {
        return silhouetteImage;
    }

    public String getRevealImage() {
        return revealImage;
    }
}
