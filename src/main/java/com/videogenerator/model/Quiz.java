package com.videogenerator.model;

import java.util.List;

public class Quiz {
    private String title;
    private int videoWidth;
    private int videoHeight;
    private int fps;
    private int questionDurationSeconds;
    private int answerRevealDurationSeconds;
    private List<Question> questions;

    public String getTitle() {
        return title;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getFps() {
        return fps;
    }

    public int getQuestionDurationSeconds() {
        return questionDurationSeconds;
    }

    public int getAnswerRevealDurationSeconds() {
        return answerRevealDurationSeconds;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
