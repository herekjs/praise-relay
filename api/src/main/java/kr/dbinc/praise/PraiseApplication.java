package kr.dbinc.praise;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PraiseApplication {

    public static void main(String[] args) throws IOException {
        // SQLite는 폴더를 만들어 주지 않아서 기본 data 폴더를 먼저 만들어요.
        Files.createDirectories(Path.of("data"));
        SpringApplication.run(PraiseApplication.class, args);
    }
}
