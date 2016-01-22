package org.lirazs.gbackbone.examples.todos.client;

import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.net.LocalStorageSyncStrategy;

import java.util.List;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 16/01/2016.
 */
public class TodoList extends Collection<Todo> {

    public TodoList() {
        // Reference to this collection's model.
        registerModelClass(Todo.class);
        // Todos are sorted by their original insertion order.
        registerComparator("order");
        // Save all of the todo items under the `"todos-backbone"` namespace.
        registerSyncStrategy(new LocalStorageSyncStrategy("todos-backbone"));
    }

    /**
     * Filter down the list of all todo items that are finished.
     *
     * @return
     */
    public List<Todo> done() {
        return where(O("done", true));
    }

    /**
     * Filter down the list to only todo items that are still not finished.
     *
     * @return
     */
    public List<Todo> remaining() {
        return where(O("done", false));
    }

    /**
     * We keep the Todos in sequential order, despite being saved by unordered
     * GUID in the database. This generates the next order number for new items.
     *
     * @return
     */
    public int nextOrder() {
        if(length() == 0)
            return 1;
        return last().getInt("order") + 1;
    }
}
