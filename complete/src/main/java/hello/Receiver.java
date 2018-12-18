package hello;

import java.util.concurrent.CountDownLatch;

import com.aig.exchange.payment.message.PaymentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private final CountDownLatch latch;

    @Autowired
    public Receiver(CountDownLatch latch) {
        this.latch = latch;
    }

//    public void receiveMessage(byte[] message) {
//        LOGGER.info("Received <" + message + ">"+System.currentTimeMillis());
//        latch.countDown();
//    }
    public void receiveMessage(PaymentMessage.PaymentFeedback pb) {
        LOGGER.info("Received <" + pb.getBankRef() + ">"+System.currentTimeMillis());
        latch.countDown();
    }
}
