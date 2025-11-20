package fit.test_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "fit.test_order_service.client")
public class TestOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestOrderServiceApplication.class, args);
    }

}
