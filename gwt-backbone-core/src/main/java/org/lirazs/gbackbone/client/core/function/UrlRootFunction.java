package org.lirazs.gbackbone.client.core.function;

import org.lirazs.gbackbone.client.core.model.Model;

public interface UrlRootFunction<V extends Model> {

    String f(V model);
}
