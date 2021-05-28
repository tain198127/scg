package org.danebrown.mode;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danebrown on 2021/2/26
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Data
public class SentinelPredicateGroup {
    /**
     * 用来匹配报文头
     */
    private List<SentinelParamPredicateConfigItem> predicate = new ArrayList<>();
    /**
     * 生成sentinel 的 resource 的报文头的key
     */
    private List<String> resourceKeys = new ArrayList<>();
    /**
     * 定义originKey
     */
    private String originKey;


}
