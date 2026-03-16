package io.github.maksluczak.ems.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class S3Buckets {

    private String employee;

    public S3Buckets() {
        this.employee = "fs-maksluczak-client-test";
    }
}
