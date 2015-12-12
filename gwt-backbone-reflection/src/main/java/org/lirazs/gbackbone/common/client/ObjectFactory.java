package org.lirazs.gbackbone.common.client;

public interface ObjectFactory<T extends Object> {
  public T getObject();
}
