package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;

import com.ventrax.support.infrastructure.files.PhotoStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

class PhotoTest {
    @TempDir Path folder;

    @Test
    void rejectsNonImage() {
        var store = new PhotoStorage(folder.toString());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        store.save(
                                new MockMultipartFile(
                                        "file", "a.png", "image/png", "bad".getBytes())));
    }

    @Test
    void storesAndReadsActualImage() throws Exception {
        var out = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
                "png",
                out);
        var store = new PhotoStorage(folder.toString());
        String key =
                store.save(
                        new MockMultipartFile(
                                "file", "../../a.png", "image/png", out.toByteArray()));
        assertArrayEquals(out.toByteArray(), new PhotoStorage(folder.toString()).read(key));
        store.delete(key);
        assertFalse(java.nio.file.Files.exists(folder.resolve(key)));
    }

    @Test
    void rejectsTraversal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhotoStorage(folder.toString()).read("../secret"));
    }
}
