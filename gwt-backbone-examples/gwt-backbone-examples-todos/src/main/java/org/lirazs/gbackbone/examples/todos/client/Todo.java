package org.lirazs.gbackbone.examples.todos.client;

import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Our basic **Todo** model has `title`, `order`, and `done` attributes.
 */
public class Todo extends Model {

    public Todo(Options attributes, Options options) {
        super(attributes, options);
    }

    /**
     * Default attributes for the todo item.
     *
     * @return
     */
    @Override
    protected Options defaults() {
        TodoList collection = (TodoList) getCollection();
        return O(
                "title", "empty todo...",
                "order", collection.nextOrder(),
                "done", false
        );
    }

    /**
     * Toggle the `done` state of this todo item.
     */
    public void toggle() {
        save(O("done", !getBoolean("done")));
    }
}
