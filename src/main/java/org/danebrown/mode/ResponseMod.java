package org.danebrown.mode;

import lombok.Data;

/**
 * Created by danebrown on 2021/2/27
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Data
public class ResponseMod {
    private String code = "";
    private String body = "";
    private String errorCode = "NULL";
}
