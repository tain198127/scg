package org.danebrown.handler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by danebrown on 2021/8/20
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
//        int i = 1/0;
    }
}
