package org.lirazs.gbackbone.examples.todos.client;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;
import org.lirazs.gbackbone.client.core.annotation.EventHandler;
import org.lirazs.gbackbone.client.core.annotation.InjectView;
import org.lirazs.gbackbone.client.core.annotation.ViewElement;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.collection.function.OnAddFunction;
import org.lirazs.gbackbone.client.core.collection.function.OnResetFunction;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.view.Template;
import org.lirazs.gbackbone.client.core.view.TemplateFactory;
import org.lirazs.gbackbone.client.core.view.View;

import java.util.List;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Our overall **AppView** is the top-level piece of UI.
 */
// Instead of generating a new element, bind to the existing skeleton of
// the App already present in the HTML.
@ViewElement("#todoapp")
public class AppView extends View {

    // Our template for the line of statistics at the bottom of the app.
    private Template statsTemplate;

    @InjectView("#new-todo")
    public GQuery input;

    @InjectView("#toggle-all")
    public InputElement allCheckbox;

    @InjectView("footer")
    public GQuery footer;

    @InjectView("#main")
    public GQuery main;

    public AppView(Options options) {
        super(options);
    }

    /**
     * At initialization we bind to the relevant events on the `Todos`
     * collection, when items are added or changed. Kick things off by
     * loading any preexisting todos that might be saved in *localStorage*.
     */
    @Override
    protected void initialize() {
        statsTemplate = TemplateFactory.template(GQuery.$("#stats-template"));

        listenTo(getCollection(), "add", new OnAddFunction<Todo>() {
            @Override
            public void f(Todo model, Collection collection, Options options) {
                addOne(model);
            }
        });
        listenTo(getCollection(), "reset", new OnResetFunction() {
            @Override
            public void f(Collection collection, Options options) {
                addAll();
            }
        });
        listenTo(getCollection(), "all", new Function() {
            @Override
            public void f() {
                render();
            }
        });

        getCollection().fetch();
    }

    /**
     * Re-rendering the App just means refreshing the statistics -- the rest
     * of the app doesn't change.
     */
    @Override
    public View render() {
        TodoList todos = (TodoList) getCollection();
        int done = todos.done().size();
        int remaining = todos.remaining().size();

        if(todos.length() > 0) {
            main.show();
            footer.show();
            footer.html(statsTemplate.apply(O("done", done, "remaining", remaining)));
        } else {
            main.hide();
            footer.hide();
        }

        allCheckbox.setChecked(remaining == 0);

        return this;
    }

    /**
     * Add a single todo item to the list by creating a view for it, and
     * appending its element to the `<ul>`.
     */
    private void addOne(Todo todo) {
        View view = new TodoView(O("model", todo));
        $("#todo-list").append(view.render().getEl());
    }

    /**
     * Add all items in the **Todos** collection at once.
     */
    private void addAll() {
        TodoList todos = (TodoList) getCollection();
        for (Todo todo : todos) {
            addOne(todo);
        }
    }

    /**
     * If you hit return in the main input field, create new **Todo** model,
     * persisting it to *localStorage*.
     */
    @EventHandler("keypress #new-todo")
    public void createOnEnter(Event event) {
        if(event.getKeyCode() != 13) return;
        if(input.val() == null || input.val().isEmpty()) return;

        getCollection().create(O("title", input.val()));
        input.val("");
    }

    /**
     * Clear all done todo items, destroying their models.
     */
    @EventHandler("click #clear-completed")
    public void clearCompleted() {
        TodoList todos = (TodoList) getCollection();
        List<Todo> doneTodos = todos.done();

        for (Todo todo : doneTodos) {
            todo.destroy();
        }
    }

    /**
     * Clear all done todo items, destroying their models.
     */
    @EventHandler("click #toggle-all")
    public void toggleAllComplete() {
        TodoList todos = (TodoList) getCollection();
        boolean done = allCheckbox.isChecked();

        for (Todo todo : todos) {
            todo.save(O("done", done));
        }
    }


}
