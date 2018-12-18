package hello;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class PbSerializer<T extends GeneratedMessage> implements RedisSerializer<T> {

  private final com.google.protobuf.Parser<T> parser;

  public PbSerializer(Parser<T> PARSER) {
    this.parser = PARSER;
  }


  @Override
  public byte[] serialize(T t) throws SerializationException {
    return t.toByteArray();
  }

  @Override
  public T deserialize(byte[] bytes) throws SerializationException {
    try {
      return parser.parseFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new SerializationException(e.getMessage());
    }
  }
}
