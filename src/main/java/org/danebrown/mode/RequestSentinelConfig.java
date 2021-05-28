package org.danebrown.mode;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danebrown on 2021/2/26
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Data
@Component
@ConfigurationProperties(prefix = "sentinel")
public class RequestSentinelConfig {
    /**
     * 拼接sentinel 的resource key的分隔符
     */
    private String delimiter = "_";
    private List<SentinelPredicateGroup> predicateGroup = new ArrayList<>();
}
