// S3Uploader.java
package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // MultipartFile을 전달받아 S3에 업로드
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }

    // [수정] S3에 업로드할 때도 고유한 파일 이름을 사용하도록 수정합니다.
    private String upload(File uploadFile, String dirName) {
        // convert 메소드에서 생성한 고유한 이름을 그대로 사용합니다.
        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                // new PutObjectRequest(...) 뒤에 있던 .withCannedAcl(...) 부분을 삭제합니다.
                new PutObjectRequest(bucket, fileName, uploadFile)
        );
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 성공적으로 삭제되었습니다");
            // 파일 삭제 성공 로그
        } else {
            log.info("파일 삭제 실패");
            // 파일 삭제 실패 로그
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        // 파일이 비어있는 경우, 변환을 시도하지 않고 바로 빈 Optional을 반환합니다.
        if (file.isEmpty()) {
            return Optional.empty();
        }

        // [수정] 원본 파일 이름 대신 UUID를 사용하여 고유한 파일 이름을 생성합니다.
        // 이렇게 하면 다른 파일과 이름이 충돌할 일이 절대 없습니다.
        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "." + extractExt(originalFilename);

        File convertFile = new File(System.getProperty("java.io.tmpdir") + "/" + storedFileName);

        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    // [추가] 파일의 확장자를 추출하는 헬퍼 메소드
    private String extractExt(String originalFilename) {
        if (originalFilename != null) {
            int pos = originalFilename.lastIndexOf(".");
            if (pos != -1 && pos < originalFilename.length() - 1) {
                return originalFilename.substring(pos + 1);
            }
        }
        return ""; // 확장자가 없는 경우
    }


}
