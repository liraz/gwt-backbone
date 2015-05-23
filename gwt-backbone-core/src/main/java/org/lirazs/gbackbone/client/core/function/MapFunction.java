package org.lirazs.gbackbone.client.core.function;

import org.lirazs.gbackbone.client.core.model.Model;

import java.util.List;

public interface MapFunction<K, V extends Model> {

    K f(V model, int index, List<V> models);
}
