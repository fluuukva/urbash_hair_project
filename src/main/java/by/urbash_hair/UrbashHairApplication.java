package by.urbash_hair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UrbashHairApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbashHairApplication.class, args);
    }
}