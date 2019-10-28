package demo.service;


import demo.instrument.Traced;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Huabing Zhao
 */
@Component
public class BankTransaction {

    @Traced
    public String toWho() {
        try {
            Thread.sleep((long) (Math.random() * 100));
            return UUID.randomUUID().toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Traced
    public void transfer() {
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}