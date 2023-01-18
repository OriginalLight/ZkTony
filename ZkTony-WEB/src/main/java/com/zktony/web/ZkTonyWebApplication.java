package com.zktony.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.**.mapper")
public class ZkTonyWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkTonyWebApplication.class, args);
    }

}
