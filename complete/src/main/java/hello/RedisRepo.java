package hello;

import java.util.Map;

public interface RedisRepo<T> {

  Map<Object, Object> findAllMovies();

  void add(T movie);

  void delete(String id);

  T findMovie(String id);

}
