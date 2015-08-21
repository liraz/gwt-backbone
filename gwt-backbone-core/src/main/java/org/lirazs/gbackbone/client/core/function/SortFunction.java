package org.lirazs.gbackbone.client.core.function;

import org.lirazs.gbackbone.client.core.model.Model;

import java.util.List;

public interface SortFunction<K, V extends Model> {

    K f(V model);
}
