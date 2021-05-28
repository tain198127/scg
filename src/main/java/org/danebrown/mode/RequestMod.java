package org.danebrown.mode;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by danebrown on 2021/2/25
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Data
public class RequestMod {
    private Map<String,String> header = new HashMap<>();
    public String getOrigin(){
        Map<String,String> map = Optional.of(header).orElse(new HashMap<>());
        return Optional.of(map.get("channel")).orElse("");
    }
    private Map<String,Object> body = new HashMap<>();
}
