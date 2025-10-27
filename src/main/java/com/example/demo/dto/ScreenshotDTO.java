package com.example.demo.dto;

public class ScreenshotDTO {
    private String fileName;
    private String base64;

    public ScreenshotDTO(String fileName, String base64) {
        this.fileName = fileName;
        this.base64 = base64;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBase64() {
        return base64;
    }
}
