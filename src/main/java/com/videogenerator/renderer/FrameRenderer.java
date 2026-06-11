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
import java.util.Random;

public class FrameRenderer {

    private final int width;
    private final int height;
    private final File imagesDir;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private final Font titleFont;
    private final Font optionsFont;

    private static final Color BG_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color OPTION_BG = new Color(255, 255, 255, 255);
    private static final Color OPTION_BORDER = new Color(200, 200, 200);
    private static final Color OPTION_SHADOW = new Color(0, 0, 0, 60);
    private static final Color CORRECT_BG = new Color(46, 204, 113);
    private static final Color CORRECT_BORDER = new Color(39, 174, 96);
    private static final Color WRONG_BG = new Color(231, 76, 60);
    private static final Color WRONG_BORDER = new Color(192, 57, 43);
    private static final Color TIMER_BAR_BG = new Color(255, 255, 255, 50);
    private static final Color TIMER_BAR_FILL = new Color(52, 152, 219);
    private static final Color TIMER_BAR_URGENT = new Color(231, 76, 60);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_BLACK = new Color(30, 30, 30);
    private static final Color TEXT_SHADOW = new Color(0, 0, 0, 150);
    private static final Color QUESTION_BOX_BG = new Color(255, 255, 255);
    private static final Color HEADER_BG = new Color(0, 0, 0, 120);

    private static final String[] OPTION_LETTERS = {"A", "B", "C", "D"};

    public FrameRenderer(int width, int height, File imagesDir) {
        this.width = width;
        this.height = height;
        this.imagesDir = imagesDir;
        this.titleFont = loadFont("fonts/frankfurter-highlight-std.otf");
        this.optionsFont = loadFont("fonts/handelson-five.otf");
    }

    private Font loadFont(String path) {
        try {
            File fontFile = new File(path);
            if (fontFile.exists()) {
                return Font.createFont(Font.TRUETYPE_FONT, fontFile);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar fonte: " + path + " - " + e.getMessage());
        }
        return new Font("SansSerif", Font.PLAIN, 1);
    }

    private Font title(int size) {
        return titleFont.deriveFont(Font.PLAIN, (float) size);
    }

    private Font options(int size) {
        return optionsFont.deriveFont(Font.PLAIN, (float) size);
    }

    public BufferedImage renderQuestionFrame(Question question, int questionNumber, int totalQuestions,
                                             double secondsRemaining, int totalSeconds, int questionIndex) {
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = frame.createGraphics();
        setupRenderingHints(g);

        if ("find-the-error".equals(question.getType())) {
            drawBackground(g, "background-3.jpg");

            drawTimer(g, secondsRemaining, totalSeconds);
            drawQuestionText(g, question.getText());
            drawImageGrid(g, question, questionIndex, false);
        } else if ("true-or-false".equals(question.getType())) {
            drawBackground(g, "background-2.jpg");

            drawTimer(g, secondsRemaining, totalSeconds);
            drawQuestionText(g, question.getText());
            // Espaço do meio vazio durante a pergunta.
        } else if ("who-is-the-character".equals(question.getType())) {
            drawBackground(g, "background-2.jpg");

            drawTimer(g, secondsRemaining, totalSeconds);
            drawQuestionText(g, question.getText());
            drawCenteredImage(g, question.getSilhouetteImage());
        } else {
            drawBackground(g, "background.jpg");

            drawTimer(g, secondsRemaining, totalSeconds);
            drawQuestionText(g, question.getText());
            boolean hasImage = question.getImage() != null && !question.getImage().isEmpty();
            if (hasImage) {
                int optionWidth = (int) ((width - scale(200)) * 0.70);
                int optionsRight = drawOptions(g, question.getOptions(), -1, scale(100), optionWidth);
                drawQuestionImage(g, question.getImage(), optionsRight);
            } else {
                int optionWidth = (int) (width * 0.40);
                int startX = (width - optionWidth) / 2;
                drawOptions(g, question.getOptions(), -1, startX, optionWidth);
            }
        }

        g.dispose();
        return frame;
    }

    public BufferedImage renderAnswerFrame(Question question, int questionNumber, int totalQuestions, int questionIndex) {
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = frame.createGraphics();
        setupRenderingHints(g);

        if ("find-the-error".equals(question.getType())) {
            drawBackground(g, "background-3.jpg");

            drawTimerFinished(g);
            drawQuestionText(g, question.getText());
            drawImageGrid(g, question, questionIndex, true);
        } else if ("true-or-false".equals(question.getType())) {
            drawBackground(g, "background-2.jpg");

            drawTimerFinished(g);
            drawQuestionText(g, question.getText());
            boolean isTrue = "true".equalsIgnoreCase(question.getAnswer());
            drawCenteredTransparentImage(g, isTrue ? "true.jpeg" : "false.jpeg");
        } else if ("who-is-the-character".equals(question.getType())) {
            drawBackground(g, "background-2.jpg");

            drawTimerFinished(g);
            drawQuestionText(g, question.getText());
            drawCenteredImage(g, question.getRevealImage());
        } else {
            drawBackground(g, "background.jpg");

            drawTimerFinished(g);
            drawQuestionText(g, question.getText());
            boolean hasImage = question.getImage() != null && !question.getImage().isEmpty();
            int correctIdx = question.hasCorrectAnswer() ? question.getCorrectIndex() : -1;
            if (hasImage) {
                int optionWidth = (int) ((width - scale(200)) * 0.70);
                int optionsRight = drawOptions(g, question.getOptions(), correctIdx, scale(100), optionWidth);
                drawQuestionImage(g, question.getImage(), optionsRight);
            } else {
                int optionWidth = (int) (width * 0.40);
                int startX = (width - optionWidth) / 2;
                drawOptions(g, question.getOptions(), correctIdx, startX, optionWidth);
            }
        }

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

        g.setFont(title(scale(28)));
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
        int boxWidth = (int) (width * 0.60);
        int boxX = (width - boxWidth) / 2;
        int boxY = scale(100);
        int boxHeight = scale(180);
        int arcSize = scale(20);
        int padding = scale(30);

        g.setColor(QUESTION_BOX_BG);
        g.fill(new RoundRectangle2D.Double(boxX, boxY, boxWidth, boxHeight, arcSize, arcSize));

        g.setFont(title(scale(42)));
        FontMetrics fm = g.getFontMetrics();

        List<String> lines = wrapText(text, fm, boxWidth - padding * 2);
        int totalTextHeight = lines.size() * fm.getHeight();
        int startY = boxY + (boxHeight - totalTextHeight) / 2 + fm.getAscent();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineX = boxX + (boxWidth - fm.stringWidth(line)) / 2;
            int lineY = startY + i * fm.getHeight();

            g.setColor(TEXT_BLACK);
            g.drawString(line, lineX, lineY);
        }
    }

    private void drawQuestionImage(Graphics2D g, String imageName, int imgStartX) {
        BufferedImage img = loadImage(imageName);
        int imgX = imgStartX;
        int imgY = scale(340);
        int imgW = width - imgStartX - scale(100);
        int imgH = height - scale(140) - imgY;
        int arcSize = scale(15);

        Shape clip = new RoundRectangle2D.Double(imgX, imgY, imgW, imgH, arcSize, arcSize);
        Shape oldClip = g.getClip();
        g.setClip(clip);

        if (img != null) {
            double scaleX = (double) imgW / img.getWidth();
            double scaleY = (double) imgH / img.getHeight();
            double sc = Math.max(scaleX, scaleY);
            int scaledW = (int) (img.getWidth() * sc);
            int scaledH = (int) (img.getHeight() * sc);
            int drawX = imgX + (imgW - scaledW) / 2;
            int drawY = imgY + (imgH - scaledH) / 2;
            g.drawImage(img, drawX, drawY, scaledW, scaledH, null);
        } else {
            g.setColor(new Color(80, 80, 120));
            g.fillRect(imgX, imgY, imgW, imgH);
        }

        g.setClip(oldClip);
        g.setColor(OPTION_BORDER);
        g.setStroke(new BasicStroke(scale(3)));
        g.draw(new RoundRectangle2D.Double(imgX, imgY, imgW, imgH, arcSize, arcSize));
    }

    private int drawOptions(Graphics2D g, List<String> options, int correctIndex, int startX, int optionWidth) {
        int startY = scale(340);
        int endY = height - scale(140);
        int availableHeight = endY - startY;
        int gap = scale(18);
        int optionHeight = (availableHeight - gap * (options.size() - 1)) / options.size();
        int padding = scale(25);

        Font font = options(scale(30));
        g.setFont(font);

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

            int shadowOff = scale(4);
            RoundRectangle2D shadow = new RoundRectangle2D.Double(startX + shadowOff, y + shadowOff, optionWidth, optionHeight, scale(15), scale(15));
            g.setColor(OPTION_SHADOW);
            g.fill(shadow);

            RoundRectangle2D rect = new RoundRectangle2D.Double(startX, y, optionWidth, optionHeight, scale(15), scale(15));
            g.setColor(bgColor);
            g.fill(rect);
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(scale(2)));
            g.draw(rect);

            FontMetrics fm = g.getFontMetrics();
            String label = OPTION_LETTERS[i] + ".  " + options.get(i);
            List<String> lines = wrapText(label, fm, optionWidth - padding * 2);

            int totalTextH = lines.size() * fm.getHeight();
            int textStartY = y + (optionHeight - totalTextH) / 2 + fm.getAscent();

            Color textColor = (correctIndex >= 0) ? TEXT_WHITE : TEXT_BLACK;
            for (int l = 0; l < lines.size(); l++) {
                int textX = startX + padding;
                int textY = textStartY + l * fm.getHeight();

                g.setColor(textColor);
                g.drawString(lines.get(l), textX, textY);
            }
        }

        return startX + optionWidth + scale(25);
    }

    private void drawCenteredImage(Graphics2D g, String imageName) {
        BufferedImage img = loadImage(imageName);
        int areaTop = scale(310);
        int areaBottom = height - scale(140);
        int areaLeft = scale(100);
        int areaRight = width - scale(100);
        int areaW = areaRight - areaLeft;
        int areaH = areaBottom - areaTop;

        if (img != null) {
            double scaleX = (double) areaW / img.getWidth();
            double scaleY = (double) areaH / img.getHeight();
            double sc = Math.min(scaleX, scaleY);
            int scaledW = (int) (img.getWidth() * sc);
            int scaledH = (int) (img.getHeight() * sc);
            int x = areaLeft + (areaW - scaledW) / 2;
            int y = areaTop + (areaH - scaledH) / 2;
            int arcSize = scale(15);

            Shape clip = new RoundRectangle2D.Double(x, y, scaledW, scaledH, arcSize, arcSize);
            Shape oldClip = g.getClip();
            g.setClip(clip);
            g.drawImage(img, x, y, scaledW, scaledH, null);
            g.setClip(oldClip);

            g.setColor(OPTION_BORDER);
            g.setStroke(new BasicStroke(scale(3)));
            g.draw(new RoundRectangle2D.Double(x, y, scaledW, scaledH, arcSize, arcSize));
        }
    }

    private void drawCenteredTransparentImage(Graphics2D g, String imageName) {
        BufferedImage img = loadTransparentImage(imageName);
        if (img == null) {
            return;
        }
        int areaTop = scale(310);
        int areaBottom = height - scale(140);
        int areaLeft = scale(100);
        int areaRight = width - scale(100);
        int areaW = areaRight - areaLeft;
        int areaH = areaBottom - areaTop;

        double scaleX = (double) areaW / img.getWidth();
        double scaleY = (double) areaH / img.getHeight();
        double sc = Math.min(scaleX, scaleY);
        int scaledW = (int) (img.getWidth() * sc);
        int scaledH = (int) (img.getHeight() * sc);
        int x = areaLeft + (areaW - scaledW) / 2;
        int y = areaTop + (areaH - scaledH) / 2;
        g.drawImage(img, x, y, scaledW, scaledH, null);
    }

    // Carrega a imagem e torna o fundo (xadrez cinza/branco) transparente via
    // flood-fill a partir das bordas. O anel colorido do ícone bloqueia o
    // preenchimento, preservando o branco interno (✓ / ✗).
    private BufferedImage loadTransparentImage(String imageName) {
        String key = "transparent:" + imageName;
        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        BufferedImage src = loadImage(imageName);
        BufferedImage out = src == null ? null : makeBackgroundTransparent(src);
        imageCache.put(key, out);
        return out;
    }

    private BufferedImage makeBackgroundTransparent(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.getGraphics().drawImage(src, 0, 0, null);

        boolean[] visited = new boolean[w * h];
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();

        for (int x = 0; x < w; x++) {
            enqueueIfBackground(out, x, 0, visited, queue);
            enqueueIfBackground(out, x, h - 1, visited, queue);
        }
        for (int y = 0; y < h; y++) {
            enqueueIfBackground(out, 0, y, visited, queue);
            enqueueIfBackground(out, w - 1, y, visited, queue);
        }

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int px = p[0];
            int py = p[1];
            out.setRGB(px, py, 0x00000000);
            enqueueIfBackground(out, px + 1, py, visited, queue);
            enqueueIfBackground(out, px - 1, py, visited, queue);
            enqueueIfBackground(out, px, py + 1, visited, queue);
            enqueueIfBackground(out, px, py - 1, visited, queue);
        }
        return out;
    }

    private void enqueueIfBackground(BufferedImage img, int x, int y, boolean[] visited, java.util.ArrayDeque<int[]> queue) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (x < 0 || y < 0 || x >= w || y >= h) {
            return;
        }
        int idx = y * w + x;
        if (visited[idx]) {
            return;
        }
        visited[idx] = true;
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int gC = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int max = Math.max(r, Math.max(gC, b));
        int min = Math.min(r, Math.min(gC, b));
        // Acinzentado (baixa saturação) e claro = xadrez de fundo.
        boolean isBackground = (max - min) <= 45 && max >= 150;
        if (isBackground) {
            queue.add(new int[]{x, y});
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

    private void drawSolidBackground(Graphics2D g) {
        g.setColor(new Color(30, 30, 60));
        g.fillRect(0, 0, width, height);
    }

    private void drawImageGrid(Graphics2D g, Question question, int questionIndex, boolean showAnswer) {
        int total = question.getRepetitions();

        Random rand = new Random(questionIndex * 31L + 7);
        int incorrectPos = rand.nextInt(total);

        int gridTop = scale(310);
        int gridBottom = height - scale(110);
        int gridLeft = scale(80);
        int gridRight = width - scale(80);

        int availableWidth = gridRight - gridLeft;
        int availableHeight = gridBottom - gridTop;

        int gap = scale(12);

        BufferedImage correctImg = loadImage(question.getCorrectImage());
        BufferedImage incorrectImg = loadImage(question.getIncorrectImage());

        // Proporção real das imagens (mantida ao desenhar, sem esticar).
        BufferedImage refImg = correctImg != null ? correctImg : incorrectImg;
        double imgAspect = (refImg != null)
                ? (double) refImg.getWidth() / refImg.getHeight()
                : 1.0;

        // Escolhe a melhor fatoração linhas x colunas que preenche o grid
        // por completo (sem buracos) e maximiza o tamanho das imagens
        // respeitando a proporção, favorecendo a distribuição horizontal.
        int cols = total;
        int rows = 1;
        int imgW = 0;
        int imgH = 0;
        for (int c = 1; c <= total; c++) {
            if (total % c != 0) {
                continue;
            }
            int r = total / c;
            int cw = (availableWidth - gap * (c - 1)) / c;
            int ch = (availableHeight - gap * (r - 1)) / r;
            // Maior imagem com a proporção dada que cabe na célula cw x ch.
            int w = (int) Math.min(cw, ch * imgAspect);
            int h = (int) (w / imgAspect);
            if (h > imgH) {
                imgW = w;
                imgH = h;
                cols = c;
                rows = r;
            }
        }

        int totalGridWidth = cols * imgW + (cols - 1) * gap;
        int totalGridHeight = rows * imgH + (rows - 1) * gap;
        int offsetX = gridLeft + (availableWidth - totalGridWidth) / 2;
        int offsetY = gridTop + (availableHeight - totalGridHeight) / 2;

        for (int i = 0; i < total; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = offsetX + col * (imgW + gap);
            int y = offsetY + row * (imgH + gap);

            boolean isIncorrect = (i == incorrectPos);
            BufferedImage img = isIncorrect ? incorrectImg : correctImg;

            if (showAnswer && !isIncorrect) {
                Composite original = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                drawScaledImage(g, img, x, y, imgW, imgH);
                g.setComposite(original);
                g.setColor(OPTION_BORDER);
                g.setStroke(new BasicStroke(scale(2)));
                g.draw(new RoundRectangle2D.Double(x, y, imgW, imgH, scale(10), scale(10)));
            } else if (showAnswer && isIncorrect) {
                int zoomX = scale(8);
                int zoomY = (int) (zoomX / imgAspect);
                drawScaledImage(g, img, x - zoomX, y - zoomY, imgW + zoomX * 2, imgH + zoomY * 2);
                g.setColor(CORRECT_BG);
                g.setStroke(new BasicStroke(scale(5)));
                g.draw(new RoundRectangle2D.Double(x - zoomX, y - zoomY, imgW + zoomX * 2, imgH + zoomY * 2, scale(12), scale(12)));
            } else {
                drawScaledImage(g, img, x, y, imgW, imgH);
                g.setColor(OPTION_BORDER);
                g.setStroke(new BasicStroke(scale(2)));
                g.draw(new RoundRectangle2D.Double(x, y, imgW, imgH, scale(10), scale(10)));
            }
        }
    }

    private void drawScaledImage(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
        if (img != null) {
            g.drawImage(img, x, y, w, h, null);
        } else {
            g.setColor(new Color(80, 80, 120));
            g.fill(new RoundRectangle2D.Double(x, y, w, h, scale(10), scale(10)));
            g.setFont(title(scale(20)));
            FontMetrics fm = g.getFontMetrics();
            String text = "?";
            g.setColor(TEXT_WHITE);
            g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent()) / 2);
        }
    }

    private int scale(int value) {
        return (int) (value * (height / 1080.0));
    }
}
