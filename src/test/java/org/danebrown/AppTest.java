package org.danebrown;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@Slf4j
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testDegradeTimes() {
        List<DegradeRule> list = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("123");
        degradeRule.setCount(1);
        degradeRule.setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType());
        degradeRule.setMinRequestAmount(1);
        degradeRule.setStatIntervalMs(30000);
        degradeRule.setTimeWindow(1000);
        list.add(degradeRule);
        DegradeRuleManager.loadRules(list);
        for (int i = 0; i < 100; i++) {
            testDegrade("123",i>=20);
        }
    }

    public Entry testDegrade(String res,boolean doError) {
         Entry entry = null;
        try {
            entry = SphU.entry(res);
            if(doError){
                Tracer.traceEntry(new RuntimeException("123"), entry);

            }
        } catch (BlockException e) {
            System.out.println("被限流了");
        }
        finally {
            if(entry!=null) {
                entry.exit();
            }
        }
        return entry;
    }
}
