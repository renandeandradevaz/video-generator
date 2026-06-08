package com.videogenerator;

import com.videogenerator.encoder.VideoEncoder;
import com.videogenerator.model.Question;
import com.videogenerator.model.Quiz;
import com.videogenerator.reader.QuizReader;
import com.videogenerator.renderer.FrameRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Uso: java -jar video-generator.jar <quiz.json> <pasta-imagens> <video-saida.mp4>");
            System.out.println("Exemplo: java -jar video-generator.jar data/quiz.json images/ output/quiz.mp4");
            System.exit(1);
        }

        String jsonPath = args[0];
        String imagesPath = args[1];
        String outputPath = args[2];

        System.out.println("=== Video Quiz Generator ===");
        System.out.println("JSON: " + jsonPath);
        System.out.println("Imagens: " + imagesPath);
        System.out.println("Saída: " + outputPath);
        System.out.println();

        QuizReader reader = new QuizReader();
        Quiz quiz = reader.read(Paths.get(jsonPath));

        System.out.println("Quiz: " + quiz.getTitle());
        System.out.println("Perguntas: " + quiz.getQuestions().size());
        System.out.println("Resolução: " + quiz.getVideoWidth() + "x" + quiz.getVideoHeight());
        System.out.println("FPS: " + quiz.getFps());
        System.out.println();

        FrameRenderer renderer = new FrameRenderer(quiz.getVideoWidth(), quiz.getVideoHeight(), new File(imagesPath));

        int totalQuestions = quiz.getQuestions().size();
        int fps = quiz.getFps();
        int questionFrames = quiz.getQuestionDurationSeconds() * fps;
        int answerFrames = quiz.getAnswerRevealDurationSeconds() * fps;
        int framesPerQuestion = questionFrames + answerFrames;
        int totalFrames = framesPerQuestion * totalQuestions;

        int totalDuration = totalQuestions * (quiz.getQuestionDurationSeconds() + quiz.getAnswerRevealDurationSeconds());
        System.out.println("Duração total: " + totalDuration + "s (" + totalFrames + " frames)");
        System.out.println("Gerando vídeo...");
        System.out.println();

        long startTime = System.currentTimeMillis();
        int frameCount = 0;

        try (VideoEncoder encoder = new VideoEncoder(quiz.getVideoWidth(), quiz.getVideoHeight(), fps, new File(outputPath))) {
            for (int q = 0; q < totalQuestions; q++) {
                Question question = quiz.getQuestions().get(q);
                int questionNum = q + 1;

                System.out.print("Pergunta " + questionNum + "/" + totalQuestions + ": ");

                for (int f = 0; f < questionFrames; f++) {
                    double secondsRemaining = (double) (questionFrames - f) / fps;
                    BufferedImage frame = renderer.renderQuestionFrame(
                            question, questionNum, totalQuestions,
                            secondsRemaining, quiz.getQuestionDurationSeconds(), q
                    );
                    encoder.writeFrame(frame);
                    frameCount++;
                }

                for (int f = 0; f < answerFrames; f++) {
                    BufferedImage frame = renderer.renderAnswerFrame(question, questionNum, totalQuestions, q);
                    encoder.writeFrame(frame);
                    frameCount++;
                }

                int percent = (int) ((double) frameCount / totalFrames * 100);
                System.out.println("OK (" + percent + "%)");
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("Vídeo gerado com sucesso!");
        System.out.println("Arquivo: " + new File(outputPath).getAbsolutePath());
        System.out.println("Tempo de geração: " + (elapsed / 1000) + "s");
    }
}
