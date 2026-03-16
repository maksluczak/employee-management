package io.github.maksluczak.ems;

import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(S3Service s3Service, S3Buckets s3Buckets) {
        return args -> {

            s3Service.putObject(
                    s3Buckets.getEmployee(),
                    "foo-2/test",
                    "Hello! Welcome to my app.".getBytes()
            );

            byte[] bytes = s3Service.getObject(
                    s3Buckets.getEmployee(),
                    "foo-2/test"
            );
            System.out.println("Hooray " + new String(bytes));
        };
    }

    private static void createRandomAdminAndEmployee() {

    }
}
