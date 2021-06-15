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
@RestControllerEndpoint(id = "edp")
public class RestController {
    /**
     * 配置：management.endpoint.gateway.enabled=true # default value
     * management.endpoints.web.exposure.include=*
     * 端点：
     * GET /actuator/gateway/globalfilters
     * GET /actuator/gateway/routefilters
     * POST /actuator/gateway/refresh
     * GET /actuator/gateway/routes
     * GET /actuator/gateway/routes/{id}
     * @return
     */
    //
//
//    /actuator/gateway/globalfilters
    @GetMapping("getUser")
    public String getUser() {
        return "I am admin";
    }
}
