package org.danebrown.protocol;

import reactor.core.publisher.Flux;

/**
 * Created by danebrown on 2021/4/7
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
public interface JsonTest {
    Flux<String> test(Flux<String> json);
}



