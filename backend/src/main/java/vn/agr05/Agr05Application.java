package vn.agr05;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Điểm chạy ứng dụng AGR-05. */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class Agr05Application {

    public static void main(String[] args) {
        SpringApplication.run(Agr05Application.class, args);
    }
}
