package com.cloud.fileupload;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class AzureController {

    // 원본 형태로 파일 업로드 시 사용(upload1 방식)
    @Value("${spring.cloud.azure.storage.blob.connection-string}")
    private String connectionString;

    // Blob 파일 형태로 파일 업로드 시 사용(upload2 방식)
    // azure-blob://<컨테이너패스>/<Blob파일형태>
    @Value("azure-blob://apthome/Blob내파일.txt")
    private Resource blobFile;

    // 테스트용 페이지
    @GetMapping("/getStarted")
    public String getStarted()  {
        return "blobFile.html";
    }

    // Blob 파일 읽을 때 사용
    @GetMapping("/readBlobFile")
    public String readBlobFile() throws IOException {
        return StreamUtils.copyToString(
                this.blobFile.getInputStream(),
                Charset.defaultCharset());
    }

    // 원본 파일 형태의 업로드
    @PostMapping("/uploadOriginFile")
    public void upload(@RequestParam("fileName") MultipartFile file) throws IOException {
        // 추후 빌드 환경으로 설정 필요
        System.out.println(connectionString);
        BlobContainerClient container = new BlobContainerClientBuilder()
                // Blob파일 커넥션 스트링
                .connectionString(connectionString)
                // 파일 경로(이게 모듈별로 서로 다른지 확인 필요)
                .containerName("apthome/APTi.Web.Front/ProFile")
                .buildClient();
        // 현재 날짜 가지고 오기
        String now = new SimpleDateFormat("yyyyMMddHmsS").format(new Date());
        // 파일 이름 변경
        String fileName = now + "_" + file.getOriginalFilename();
        // 파일 객체의 파일을 Blob 컨테이너에 할당

        BlobClient blob = container.getBlobClient(fileName);
        // 파일 업로드 진행
        blob.upload(file.getInputStream(), file.getSize(), true);
        System.out.print("파일 업로드 성공");
        // To do list : 파일 저장 프로시저 호출 로직 구현

    }

    // Blob 형태의 파일 업로드
    @PostMapping("/uploadBlobFile")
    public void upload2(@RequestParam("fileName") MultipartFile file) throws IOException {

        try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
            os.write(file.getBytes());
        }
        System.out.println("파일 업로드 성공");
    }
}
