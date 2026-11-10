package com.ventrax.support.infrastructure.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Component
public class PhotoStorage implements com.ventrax.support.application.outbound.PhotoStore {
    private final Path root;

    public PhotoStorage(@Value("${smartgn.photo-path}") String path) {
        root = Path.of(path).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file) {
        try {
            if (file.getSize() > 10 * 1024 * 1024)
                throw new IllegalArgumentException("Photo exceeds 10 MiB");
            byte[] bytes = file.getBytes();
            boolean png =
                    bytes.length > 8
                            && bytes[0] == (byte) 137
                            && bytes[1] == 80
                            && bytes[2] == 78
                            && bytes[3] == 71;
            boolean jpg =
                    bytes.length > 3
                            && bytes[0] == (byte) 255
                            && bytes[1] == (byte) 216
                            && bytes[2] == (byte) 255;
            if ((!png && !jpg) || !validImage(bytes))
                throw new IllegalArgumentException("JPEG or PNG required");
            Files.createDirectories(root);
            String key = UUID.randomUUID() + (png ? ".png" : ".jpg");
            Files.write(root.resolve(key), bytes, StandardOpenOption.CREATE_NEW);
            return key;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Photo storage unavailable", e);
        }
    }

    private boolean validImage(byte[] bytes) {
        try (var input =
                new javax.imageio.stream.MemoryCacheImageInputStream(
                        new java.io.ByteArrayInputStream(bytes))) {
            var readers = javax.imageio.ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return false;
            var reader = readers.next();
            try {
                reader.setInput(input);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                return pixels > 0 && pixels <= 25_000_000 && reader.read(0) != null;
            } finally {
                reader.dispose();
            }
        } catch (java.io.IOException malformed) {
            return false;
        }
    }

    public void delete(String key) {
        try {
            Files.deleteIfExists(root.resolve(key));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Photo cleanup failed", e);
        }
    }

    public byte[] read(String key) throws java.io.IOException {
        if (!key.matches("[a-f0-9-]{36}\\.(png|jpg)"))
            throw new IllegalArgumentException("Invalid photo key");
        return Files.readAllBytes(root.resolve(key));
    }
}
