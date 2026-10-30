package com.ventrax.support.application.outbound;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStore {
    String save(MultipartFile file);

    byte[] read(String key) throws java.io.IOException;

    void delete(String key);
}
