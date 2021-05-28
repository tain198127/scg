package org.danebrown;

import com.alibaba.csp.sentinel.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Hello world!
 *
 */
@EnableConfigurationProperties
@SpringBootApplication
public class SpringGateWayApplication
{
    public static void main(String[] args) {
        Constants.ON=false;
        SpringApplication.run(SpringGateWayApplication.class, args);
    }
}
