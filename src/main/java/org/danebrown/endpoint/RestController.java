package org.danebrown.endpoint;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by danebrown on 2021/6/4
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Component
@RestControllerEndpoint(id = "test")
public class RestController {
    @GetMapping("getUser")
    public String getUser() {
        return "I am admin";
    }
}
