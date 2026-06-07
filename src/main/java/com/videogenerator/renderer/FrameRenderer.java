package com.videogenerator.renderer;

import com.videogenerator.model.Question;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameRenderer {

    private final int width;
    private final int height;
    private final File imagesDir;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    private static final Color BG_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color OPTION_BG = new Color(255, 255, 255, 40);
    private static final Color OPTION_BORDER = new Color(255, 255, 255, 100);
    private static final Color CORRECT_BG = new Color(46, 204, 113, 200);
    private static final Color CORRECT_BORDER = new Color(39, 174, 96);
    private static final Color WRONG_BG = new Color(231, 76, 60, 120);
    private static final Color WRONG_BORDER = new Color(192, 57, 43, 150);
    private static final Color TIMER_BAR_BG = new Color(255, 255, 255, 50);
    private static final Color TIMER_BAR_FILL = new Color(52, 152, 219);
    private static final Color TIMER_BAR_URGENT = new Color(231, 76, 60);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_SHADOW = new Color(0, 0, 0, 150);
    private static final Color QUESTION_BOX_BG = new Color(0, 0, 0, 140);
    private static final Color HEADER_BG = new Color(0, 0, 0, 120);

    private static final String[] OPTION_LETTERS = {"A", "B", "C", "D"};

    public FrameRenderer(int width, int height, File imagesDir) {
        this.width = width;
        this.height = height;
        this.imagesDir = imagesDir;
    }

    public BufferedImage renderQuestionFrame(Question question, int questionNumber, int totalQuestions,
                                             double secondsRemaining, int totalSeconds) {
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = frame.createGraphics();
        setupRenderingHints(g);

        drawBackground(g, question.getImage());
        drawOverlay(g);
        drawHeader(g, questionNumber, totalQuestions);
        drawTimer(g, secondsRemaining, totalSeconds);
        drawQuestionText(g, question.getText());
        drawOptions(g, question.getOptions(), -1);

        g.dispose();
        return frame;
    }

    public BufferedImage renderAnswerFrame(Question question, int questionNumber, int totalQuestions) {
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = frame.createGraphics();
        setupRenderingHints(g);

        drawBackground(g, question.getImage());
        drawOverlay(g);
        drawHeader(g, questionNumber, totalQuestions);
        drawTimerFinished(g);
        drawQuestionText(g, question.getText());
        drawOptions(g, question.getOptions(), question.getCorrectIndex());

        g.dispose();
        return frame;
    }

    private void setupRenderingHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    private void drawBackground(Graphics2D g, String imageName) {
        BufferedImage bg = loadImage(imageName);
        if (bg != null) {
            double scaleX = (double) width / bg.getWidth();
            double scaleY = (double) height / bg.getHeight();
            double scale = Math.max(scaleX, scaleY);
            int scaledW = (int) (bg.getWidth() * scale);
            int scaledH = (int) (bg.getHeight() * scale);
            int x = (width - scaledW) / 2;
            int y = (height - scaledH) / 2;
            g.drawImage(bg, x, y, scaledW, scaledH, null);
        } else {
            g.setColor(new Color(30, 30, 60));
            g.fillRect(0, 0, width, height);
        }
    }

    private void drawOverlay(Graphics2D g) {
        g.setColor(BG_OVERLAY);
        g.fillRect(0, 0, width, height);
    }

    private void drawHeader(Graphics2D g, int questionNumber, int totalQuestions) {
        int headerHeight = scale(70);
        g.setColor(HEADER_BG);
        g.fillRect(0, 0, width, headerHeight);

        g.setFont(new Font("SansSerif", Font.BOLD, scale(28)));
        String headerText = "Pergunta " + questionNumber + " de " + totalQuestions;
        FontMetrics fm = g.getFontMetrics();
        int textX = (width - fm.stringWidth(headerText)) / 2;
        int textY = (headerHeight + fm.getAscent() - fm.getDescent()) / 2;

        g.setColor(TEXT_SHADOW);
        g.drawString(headerText, textX + 2, textY + 2);
        g.setColor(TEXT_WHITE);
        g.drawString(headerText, textX, textY);
    }

    private void drawTimer(Graphics2D g, double secondsRemaining, int totalSeconds) {
        int barX = scale(100);
        int barY = height - scale(80);
        int barWidth = width - scale(200);
        int barHeight = scale(16);
        int arcSize = scale(8);

        g.setColor(TIMER_BAR_BG);
        g.fill(new RoundRectangle2D.Double(barX, barY, barWidth, barHeight, arcSize, arcSize));

        double progress = secondsRemaining / totalSeconds;
        int fillWidth = (int) (barWidth * progress);
        Color fillColor = secondsRemaining <= 3 ? TIMER_BAR_URGENT : TIMER_BAR_FILL;
        g.setColor(fillColor);
        g.fill(new RoundRectangle2D.Double(barX, barY, fillWidth, barHeight, arcSize, arcSize));

        g.setFont(new Font("SansSerif", Font.BOLD, scale(36)));
        String timerText = String.valueOf((int) Math.ceil(secondsRemaining));
        FontMetrics fm = g.getFontMetrics();
        int timerX = (width - fm.stringWidth(timerText)) / 2;
        int timerY = barY - scale(15);

        g.setColor(TEXT_SHADOW);
        g.drawString(timerText, timerX + 2, timerY + 2);
        g.setColor(secondsRemaining <= 3 ? TIMER_BAR_URGENT : TEXT_WHITE);
        g.drawString(timerText, timerX, timerY);
    }

    private void drawTimerFinished(Graphics2D g) {
        int barX = scale(100);
        int barY = height - scale(80);
        int barWidth = width - scale(200);
        int barHeight = scale(16);
        int arcSize = scale(8);

        g.setColor(TIMER_BAR_BG);
        g.fill(new RoundRectangle2D.Double(barX, barY, barWidth, barHeight, arcSize, arcSize));

        g.setFont(new Font("SansSerif", Font.BOLD, scale(36)));
        String timerText = "✓";
        FontMetrics fm = g.getFontMetrics();
        int timerX = (width - fm.stringWidth(timerText)) / 2;
        int timerY = barY - scale(15);

        g.setColor(CORRECT_BG);
        g.drawString(timerText, timerX, timerY);
    }

    private void drawQuestionText(Graphics2D g, String text) {
        int boxX = scale(80);
        int boxY = scale(100);
        int boxWidth = width - scale(160);
        int boxHeight = scale(180);
        int arcSize = scale(20);
        int padding = scale(30);

        g.setColor(QUESTION_BOX_BG);
        g.fill(new RoundRectangle2D.Double(boxX, boxY, boxWidth, boxHeight, arcSize, arcSize));

        g.setFont(new Font("SansSerif", Font.BOLD, scale(36)));
        FontMetrics fm = g.getFontMetrics();

        List<String> lines = wrapText(text, fm, boxWidth - padding * 2);
        int totalTextHeight = lines.size() * fm.getHeight();
        int startY = boxY + (boxHeight - totalTextHeight) / 2 + fm.getAscent();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineX = boxX + (boxWidth - fm.stringWidth(line)) / 2;
            int lineY = startY + i * fm.getHeight();

            g.setColor(TEXT_SHADOW);
            g.drawString(line, lineX + 2, lineY + 2);
            g.setColor(TEXT_WHITE);
            g.drawString(line, lineX, lineY);
        }
    }

    private void drawOptions(Graphics2D g, List<String> options, int correctIndex) {
        int optionWidth = width - scale(200);
        int optionHeight = scale(80);
        int gap = scale(18);
        int startX = scale(100);
        int startY = scale(340);

        g.setFont(new Font("SansSerif", Font.BOLD, scale(30)));

        for (int i = 0; i < options.size(); i++) {
            int y = startY + i * (optionHeight + gap);

            Color bgColor;
            Color borderColor;
            if (correctIndex >= 0) {
                if (i == correctIndex) {
                    bgColor = CORRECT_BG;
                    borderColor = CORRECT_BORDER;
                } else {
                    bgColor = WRONG_BG;
                    borderColor = WRONG_BORDER;
                }
            } else {
                bgColor = OPTION_BG;
                borderColor = OPTION_BORDER;
            }

            RoundRectangle2D rect = new RoundRectangle2D.Double(startX, y, optionWidth, optionHeight, scale(15), scale(15));
            g.setColor(bgColor);
            g.fill(rect);
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(scale(3)));
            g.draw(rect);

            FontMetrics fm = g.getFontMetrics();
            String label = OPTION_LETTERS[i] + ".  " + options.get(i);
            int textX = startX + scale(25);
            int textY = y + (optionHeight + fm.getAscent() - fm.getDescent()) / 2;

            g.setColor(TEXT_SHADOW);
            g.drawString(label, textX + 2, textY + 2);
            g.setColor(TEXT_WHITE);
            g.drawString(label, textX, textY);
        }
    }

    private BufferedImage loadImage(String imageName) {
        if (imageCache.containsKey(imageName)) {
            return imageCache.get(imageName);
        }
        try {
            File file = new File(imagesDir, imageName);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                imageCache.put(imageName, img);
                return img;
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar imagem: " + imageName + " - " + e.getMessage());
        }
        imageCache.put(imageName, null);
        return null;
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String test = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(test) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private int scale(int value) {
        return (int) (value * (height / 1080.0));
    }
}
