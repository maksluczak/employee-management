package io.github.maksluczak.ems.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3.buckets")
public record S3Buckets(String employee) {
}