package org.lirazs.gbackbone.examples.todos.client;

import com.google.gwt.core.client.EntryPoint;
import org.lirazs.gbackbone.client.core.collection.Collection;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 11/01/2016.
 */
public class TodosEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TodoList todos = new TodoList();

        AppView appView = new AppView(O("collection", todos));
    }
}
