package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ImageConverterService {

    /**
     * 이미지를 RGB 형식 JPG로 변환 (Alpha 채널 제거)
     * AI 모델이 요구하는 3채널 (RGB) 형식으로 변환합니다.
     */
    public ByteArrayResource convertToRGB(MultipartFile imageFile) throws IOException {
        log.info("Converting image to RGB format: {} ({})",
                imageFile.getOriginalFilename(), imageFile.getContentType());

        // 원본 이미지 읽기
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageFile.getBytes()));

        if (originalImage == null) {
            throw new IOException("이미지를 읽을 수 없습니다: " + imageFile.getOriginalFilename());
        }

        // 이미지 정보 로깅
        log.info("Original image info - Width: {}, Height: {}, Type: {}, ColorModel: {}",
                originalImage.getWidth(), originalImage.getHeight(),
                originalImage.getType(), originalImage.getColorModel());

        // RGB 형식으로 변환
        BufferedImage rgbImage;

        if (originalImage.getType() == BufferedImage.TYPE_INT_ARGB ||
                originalImage.getType() == BufferedImage.TYPE_4BYTE_ABGR ||
                originalImage.getColorModel().hasAlpha()) {

            log.info("Converting RGBA to RGB (removing alpha channel)");

            // 알파 채널이 있는 경우 RGB로 변환
            rgbImage = new BufferedImage(originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            // 흰색 배경으로 알파 채널 처리
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

        } else if (originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {

            log.info("Converting Grayscale to RGB");

            // 그레이스케일을 RGB로 변환
            rgbImage = new BufferedImage(originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

        } else {
            log.info("Image is already in RGB format");
            rgbImage = originalImage;
        }

        // JPG로 저장 (JPG는 알파 채널을 지원하지 않으므로 확실히 RGB가 됨)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean success = ImageIO.write(rgbImage, "jpg", outputStream);

        if (!success) {
            throw new IOException("JPG 형식으로 변환할 수 없습니다");
        }

        byte[] convertedBytes = outputStream.toByteArray();
        log.info("Image converted successfully. Original size: {} bytes, Converted size: {} bytes",
                imageFile.getSize(), convertedBytes.length);

        // ByteArrayResource 생성
        return new ByteArrayResource(convertedBytes) {
            @Override
            public String getFilename() {
                String originalName = imageFile.getOriginalFilename();
                if (originalName == null) {
                    return "converted_image.jpg";
                }

                // 확장자를 .jpg로 변경
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    return originalName.substring(0, dotIndex) + ".jpg";
                } else {
                    return originalName + ".jpg";
                }
            }

            @Override
            public long contentLength() {
                return convertedBytes.length;
            }
        };
    }

    /**
     * 이미지 크기를 조정 (선택사항 - AI 모델이 특정 크기를 요구하는 경우)
     */
    public ByteArrayResource resizeAndConvertToRGB(MultipartFile imageFile, int maxWidth, int maxHeight) throws IOException {
        log.info("Resizing and converting image: {} to max {}x{}",
                imageFile.getOriginalFilename(), maxWidth, maxHeight);

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageFile.getBytes()));

        if (originalImage == null) {
            throw new IOException("이미지를 읽을 수 없습니다");
        }

        // 비율 유지하며 크기 계산
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        log.info("Resizing from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);

        // 크기 조정된 RGB 이미지 생성
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // 고품질 리샘플링
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 흰색 배경
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);

        // 이미지 그리기
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // JPG로 저장
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);

        byte[] convertedBytes = outputStream.toByteArray();
        log.info("Image resized and converted. Size: {} bytes", convertedBytes.length);

        return new ByteArrayResource(convertedBytes) {
            @Override
            public String getFilename() {
                String originalName = imageFile.getOriginalFilename();
                String baseName = originalName != null ? originalName.replaceAll("\\.[^.]*$", "") : "resized_image";
                return baseName + "_resized.jpg";
            }

            @Override
            public long contentLength() {
                return convertedBytes.length;
            }
        };
    }
}
