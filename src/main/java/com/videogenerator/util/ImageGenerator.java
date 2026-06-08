package com.videogenerator.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ImageGenerator {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    private static final Color[][] PALETTES = {
            {new Color(255, 107, 53), new Color(209, 50, 48)},     // orange-red
            {new Color(41, 128, 185), new Color(44, 62, 80)},      // blue-dark
            {new Color(241, 196, 15), new Color(243, 156, 18)},    // yellow-orange
            {new Color(155, 89, 182), new Color(142, 68, 173)},    // purple
            {new Color(46, 204, 113), new Color(39, 174, 96)},     // green
            {new Color(231, 76, 60), new Color(192, 57, 43)},      // red
            {new Color(52, 152, 219), new Color(41, 128, 185)},    // light-blue
            {new Color(230, 126, 34), new Color(211, 84, 0)},      // dark-orange
            {new Color(26, 188, 156), new Color(22, 160, 133)},    // teal
            {new Color(52, 73, 94), new Color(44, 62, 80)},        // dark-navy
    };

    public static void main(String[] args) throws IOException {
        File outputDir = new File(args.length > 0 ? args[0] : "images");
        outputDir.mkdirs();

        for (int i = 0; i < 10; i++) {
            BufferedImage img = generateImage(i, PALETTES[i]);
            File output = new File(outputDir, String.format("question_%02d.jpg", i + 1));
            ImageIO.write(img, "jpg", output);
            System.out.println("Gerada: " + output.getName());
        }

        generateFindTheErrorPairs(outputDir);

        System.out.println("Imagens geradas em: " + outputDir.getAbsolutePath());
    }

    private static void generateFindTheErrorPairs(File outputDir) throws IOException {
        // Pair 1: circles pattern - incorrect has a square instead of a circle
        generatePair(outputDir, 1, PALETTES[0], PALETTES[1]);
        // Pair 2: stars pattern - incorrect has different colored star
        generatePair(outputDir, 2, PALETTES[4], PALETTES[6]);
        System.out.println();
    }

    private static void generatePair(File outputDir, int pairIndex, Color[] palette, Color[] altPalette) throws IOException {
        BufferedImage correct = generateFindErrorImage(pairIndex, palette, false);
        BufferedImage incorrect = generateFindErrorImage(pairIndex, palette, true);

        File correctFile = new File(outputDir, String.format("correct_%02d.jpg", pairIndex));
        File incorrectFile = new File(outputDir, String.format("incorrect_%02d.jpg", pairIndex));
        ImageIO.write(correct, "jpg", correctFile);
        ImageIO.write(incorrect, "jpg", incorrectFile);
        System.out.println("Par gerado: " + correctFile.getName() + " / " + incorrectFile.getName());
    }

    private static BufferedImage generateFindErrorImage(int seed, Color[] palette, boolean withError) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, palette[0], WIDTH, HEIGHT, palette[1]);
        g.setPaint(gradient);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Random rand = new Random(seed * 99L);

        for (int i = 0; i < 6; i++) {
            int cx = 200 + rand.nextInt(WIDTH - 400);
            int cy = 200 + rand.nextInt(HEIGHT - 400);
            int size = 80 + rand.nextInt(120);

            g.setColor(new Color(255, 255, 255, 50 + rand.nextInt(40)));

            if (withError && i == 3) {
                g.fillRect(cx - size / 2, cy - size / 2, size, size);
            } else {
                g.fill(new Ellipse2D.Double(cx - size / 2, cy - size / 2, size, size));
            }
        }

        int centerSize = 150;
        int cx = WIDTH / 2;
        int cy = HEIGHT / 2;

        if (withError) {
            g.setColor(new Color(231, 76, 60, 180));
        } else {
            g.setColor(new Color(46, 204, 113, 180));
        }
        drawStar(g, cx, cy, centerSize, new Random(seed));

        g.setColor(new Color(255, 255, 255, 30));
        g.setStroke(new BasicStroke(3));
        for (int i = 0; i < 4; i++) {
            int x1 = rand.nextInt(WIDTH);
            int y1 = rand.nextInt(HEIGHT);
            g.drawLine(x1, y1, x1 + rand.nextInt(300) - 150, y1 + rand.nextInt(300) - 150);
        }

        g.dispose();
        return img;
    }

    private static BufferedImage generateImage(int seed, Color[] palette) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, palette[0], WIDTH, HEIGHT, palette[1]);
        g.setPaint(gradient);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Random rand = new Random(seed * 42L + 7);

        for (int i = 0; i < 15; i++) {
            int alpha = 20 + rand.nextInt(40);
            g.setColor(new Color(255, 255, 255, alpha));
            int size = 50 + rand.nextInt(300);
            int x = rand.nextInt(WIDTH);
            int y = rand.nextInt(HEIGHT);
            g.fill(new Ellipse2D.Double(x, y, size, size));
        }

        for (int i = 0; i < 8; i++) {
            int alpha = 15 + rand.nextInt(30);
            g.setColor(new Color(0, 0, 0, alpha));
            drawStar(g, rand.nextInt(WIDTH), rand.nextInt(HEIGHT), 30 + rand.nextInt(80), rand);
        }

        for (int i = 0; i < 5; i++) {
            int alpha = 10 + rand.nextInt(25);
            g.setColor(new Color(255, 255, 255, alpha));
            g.setStroke(new BasicStroke(2 + rand.nextInt(4)));
            int x1 = rand.nextInt(WIDTH);
            int y1 = rand.nextInt(HEIGHT);
            int x2 = x1 + rand.nextInt(400) - 200;
            int y2 = y1 + rand.nextInt(400) - 200;
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        return img;
    }

    private static void drawStar(Graphics2D g, int cx, int cy, int size, Random rand) {
        int points = 4 + rand.nextInt(3);
        Path2D path = new Path2D.Double();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI * i / points - Math.PI / 2;
            double r = (i % 2 == 0) ? size : size * 0.4;
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();
        g.fill(path);
    }
}
