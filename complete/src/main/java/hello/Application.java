package hello;

import java.util.concurrent.CountDownLatch;

import com.aig.exchange.payment.message.PaymentMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "receiveMessage"){

			@Override
			public void onMessage(Message message, byte[] pattern) {
				super.onMessage(message, pattern);
			}

			@Override
			protected Object extractMessage(Message message) {
				try {
					return PaymentMessage.PaymentFeedback.parseFrom(message.getBody());
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
					return PaymentMessage.PaymentFeedback.getDefaultInstance();
				}
			}
		};
//		adapter.setSerializer(keySerializer());
		return adapter;
	}

	@Bean
	Receiver receiver(CountDownLatch latch) {
		return new Receiver(latch);
	}

	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(1);
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	@Bean(name = "pbBean")
	RedisTemplate<String, PaymentMessage.PaymentFeedback> pbTemplate(RedisConnectionFactory connectionFactory){
		RedisTemplate<String,PaymentMessage.PaymentFeedback> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.afterPropertiesSet();
		template.setEnableDefaultSerializer(false);
		template.setKeySerializer(keySerializer());
		template.setHashKeySerializer(keySerializer());
		template.setValueSerializer(valueSerializer());
		template.setHashValueSerializer(valueSerializer());
		return template;
	}

	@Bean
	public RedisSerializer keySerializer(){
		return new GenericToStringSerializer(String.class);
	}

	@Bean
	public RedisSerializer valueSerializer(){
		return new PbSerializer(PaymentMessage.PaymentFeedback.PARSER);
	}

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		PaymentMessage.PaymentFeedback pbObject = PaymentMessage.PaymentFeedback.newBuilder().setAmount(1).setBankRef("s").setCommnent("c").setCurrency("dd").setReference("dd1").build();
		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		RedisRepo redisRepo = ctx.getBean(RedisRepositoryImpl.class);
		long start = System.nanoTime();
		redisRepo.add(pbObject);
		long end = System.nanoTime();
		System.out.println(end - start);
		PaymentMessage.PaymentFeedback readO = ((RedisRepositoryImpl) redisRepo).findMovie("dd1");
		RedisTemplate<String, PaymentMessage.PaymentFeedback> pbTemplate  = (RedisTemplate<String, PaymentMessage.PaymentFeedback>) ctx.getBean("pbBean");
//		pbTemplate.opsForHash().put("payment1","payment1",pbObject);
//		System.out.println(pbTemplate.opsForHash().get("payment1","payment1"));

		CountDownLatch latch = ctx.getBean(CountDownLatch.class);

		LOGGER.info("Sending message... {}",System.currentTimeMillis());
//		template.convertAndSend("chat", "Hello from Redis!+");
		pbTemplate.convertAndSend("chat", pbObject);
		latch.await();

		System.exit(0);
	}
}
