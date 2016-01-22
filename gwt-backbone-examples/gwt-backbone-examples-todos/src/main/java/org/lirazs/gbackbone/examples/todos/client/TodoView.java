package org.lirazs.gbackbone.examples.todos.client;

import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;
import org.lirazs.gbackbone.client.core.annotation.EventHandler;
import org.lirazs.gbackbone.client.core.annotation.InjectView;
import org.lirazs.gbackbone.client.core.annotation.ViewTagName;
import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.model.function.OnChangeFunction;
import org.lirazs.gbackbone.client.core.model.function.OnDestroyFunction;
import org.lirazs.gbackbone.client.core.view.View;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 16/01/2016.
 */
@ViewTagName("li")
@ViewTemplate(selector = "#item-template", autoRender = false)
public class TodoView extends View<Todo> {

    @InjectView(".edit")
    public GQuery input;

    public TodoView(Options options) {
        super(options);
    }

    /**
     *  The TodoView listens for changes to its model, re-rendering. Since there's
     * a one-to-one correspondence between a **Todo** and a **TodoView** in this
     * app, we set a direct reference on the model for convenience.
     */
    @Override
    protected void initialize() {
        listenTo(getModel(), "change", new OnChangeFunction() {
            @Override
            public void f(Model model, Options options) {
                render();
            }
        });
        listenTo(getModel(), "destroy", new OnDestroyFunction() {
            @Override
            public void f(Model model, Collection collection, Options options) {
                remove();
            }
        });
    }

    /**
     *  Re-render the titles of the todo item.
     */
    @Override
    public View render() {
        get$El().html(getTemplate().apply(getModel().toJSON()));
        get$El().toggleClass("done", getModel().getBoolean("done"));

        injectViews();

        return this;
    }

    /**
     * Toggle the `"done"` state of the model.
     */
    @EventHandler("click .toggle")
    public void toggleDone() {
        getModel().toggle();
    }

    /**
     * Switch this view into `"editing"` mode, displaying the input field.
     */
    @EventHandler("dblclick .view")
    public void edit() {
        get$El().addClass("editing");
        input.focus();
    }

    /**
     * Remove the item, destroy the model.
     */
    @EventHandler("click a.destroy")
    public void clear() {
        getModel().destroy();
    }

    /**
     * If you hit `enter`, we're through editing the item.
     */
    @EventHandler("keypress .edit")
    public void updateOnEnter(Event event) {
        if(event.getKeyCode() == 13) {
            close();
        }
    }

    /**
     * Close the `"editing"` mode, saving changes to the todo.
     */
    @EventHandler("blur .edit")
    public void close() {
        String value = input.val();
        if(value == null || value.isEmpty()) {
            clear();
        } else {
            getModel().save(O("title", value));
            get$El().removeClass("editing");
        }
    }
}
