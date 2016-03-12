package org.lirazs.gbackbone.examples.todos.client;

import com.google.gwt.core.client.EntryPoint;
import org.lirazs.gbackbone.client.core.collection.Collection;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 11/01/2016.
 */
public class TodosEntryPoint implements EntryPoint {
    private AppView appView;

    @Override
    public void onModuleLoad() {
        TodoList todos = new TodoList();

        //TODO: Todos application does not work!! seems like reflection is picking up GQuery & Locale class from somewhere - only on maven build
        appView = new AppView(O("collection", todos));
    }
}
