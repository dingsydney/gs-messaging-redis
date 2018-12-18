package hello;

import com.aig.exchange.payment.message.PaymentMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class RedisRepositoryImpl implements RedisRepo<PaymentMessage.PaymentFeedback> {
  private static final String KEY = "PaymentFeedback";

  private RedisTemplate<String, PaymentMessage.PaymentFeedback> redisTemplate;
  private HashOperations hashOperations;

  @Autowired
  public RedisRepositoryImpl(RedisTemplate<String, PaymentMessage.PaymentFeedback> redisTemplate){
    this.redisTemplate = redisTemplate;
  }

  @PostConstruct
  private void init(){
    hashOperations = redisTemplate.opsForHash();
  }

  public void add(final PaymentMessage.PaymentFeedback movie) {
    hashOperations.put(KEY, movie.getReference(), movie);
  }

  public void delete(final String id) {
    hashOperations.delete(KEY, id);
  }

  public PaymentMessage.PaymentFeedback findMovie(final String id){
    return (PaymentMessage.PaymentFeedback) hashOperations.get(KEY, id);
  }

  public Map<Object, Object> findAllMovies(){
    return hashOperations.entries(KEY);
  }


}