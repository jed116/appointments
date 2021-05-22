package tech.itpark.bodyconverter;

import java.io.Reader;
import java.io.Writer;

public interface BodyConverter {
  boolean canRead(String contentType, Class<?> clazz);

  boolean canWrite(String contentType, Class<?> clazz);

  <T> T read(Reader reader, Class<T> clazz);

  void write(Writer writer, Object value);
}
