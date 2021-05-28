package org.danebrown.mode;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * Created by danebrown on 2021/2/26
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Component
public class TruePredicate implements Predicate<Object> {
    @Override
    public boolean test(Object o) {
        return true;
    }
}
