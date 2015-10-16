package org.lirazs.gbackbone.client.core.function;

import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

public interface ModelClassFunction<T extends Model> {

    Class<? extends T> f(Options attributes);
}
