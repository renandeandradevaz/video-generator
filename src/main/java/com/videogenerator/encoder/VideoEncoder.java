package com.videogenerator.encoder;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

public class VideoEncoder implements Closeable {

    private final Process process;
    private final OutputStream ffmpegInput;
    private final int width;
    private final int height;

    public VideoEncoder(int width, int height, int fps, File outputFile) throws IOException {
        this.width = width;
        this.height = height;

        outputFile.getParentFile().mkdirs();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-f", "rawvideo",
                "-pixel_format", "bgr24",
                "-video_size", width + "x" + height,
                "-r", String.valueOf(fps),
                "-i", "-",
                "-c:v", "libx264",
                "-preset", "medium",
                "-crf", "23",
                "-pix_fmt", "yuv420p",
                "-movflags", "+faststart",
                outputFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        process = pb.start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // consume ffmpeg output silently
                }
            } catch (IOException ignored) {
            }
        }, "ffmpeg-output-reader").start();

        ffmpegInput = new BufferedOutputStream(process.getOutputStream(), width * height * 3 * 4);
    }

    public void writeFrame(BufferedImage frame) throws IOException {
        byte[] pixels = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
        ffmpegInput.write(pixels);
    }

    @Override
    public void close() throws IOException {
        ffmpegInput.flush();
        ffmpegInput.close();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg terminou com código de erro: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrompido enquanto aguardava FFmpeg", e);
        }
    }
}
