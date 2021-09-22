package org.danebrown.handler;

import com.alibaba.csp.sentinel.command.CommandCenterProvider;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.util.ObjectUtils;

import java.util.stream.Stream;

/**
 * Created by danebrown on 2021/8/20
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
public class GraceFulShutdownListener implements SmartApplicationListener {
    private static final Class<? extends ApplicationEvent>[] SUPPORT_APPLICATION_EVENT=
        new Class[]{ApplicationReadyEvent.class, ContextClosedEvent.class};
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ObjectUtils.containsElement(SUPPORT_APPLICATION_EVENT,eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(event instanceof ApplicationReadyEvent){

        }else if(event instanceof  ContextClosedEvent){

            try {
                CommandCenter commandCenter =
                        CommandCenterProvider.getCommandCenter();
                commandCenter.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.err.println("yep, you got a error");
        }

    }
}
