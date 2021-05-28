package org.danebrown.mode;

import lombok.Data;

/**
 * Created by danebrown on 2021/2/26
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Data
public class SentinelParamPredicateConfigItem {
    /**
     * 要识别的报文头的key
     */
    private String key;
    /**
     * key对应的值
     */
    private String condition;
}
