package com.videogenerator.reader;

import com.google.gson.Gson;
import com.videogenerator.model.Quiz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuizReader {

    public Quiz read(Path jsonPath) throws IOException {
        String json = new String(Files.readAllBytes(jsonPath));
        return new Gson().fromJson(json, Quiz.class);
    }
}
