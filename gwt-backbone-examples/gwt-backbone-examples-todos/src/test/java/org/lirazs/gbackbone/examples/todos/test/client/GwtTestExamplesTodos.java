package org.lirazs.gbackbone.examples.todos.test.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.junit.client.GWTTestCase;
import org.lirazs.gbackbone.examples.todos.client.TodosEntryPoint;

/**
 * Created on 23/10/2015.
 */
public class GwtTestExamplesTodos extends GWTTestCase {

    public String getModuleName() {
        return "org.lirazs.gbackbone.examples.todos.test.TodosTest";
    }

    public void gwtSetUp() {
        String html = "<div id=\"todoapp\">\n" +
                "        <header>\n" +
                "            <h1>Todos</h1>\n" +
                "            <input id=\"new-todo\" type=\"text\" placeholder=\"What needs to be done?\">\n" +
                "        </header>\n" +
                "\n" +
                "        <section id=\"main\">\n" +
                "            <input id=\"toggle-all\" type=\"checkbox\">\n" +
                "            <label for=\"toggle-all\">Mark all as complete</label>\n" +
                "            <ul id=\"todo-list\"></ul>\n" +
                "        </section>\n" +
                "\n" +
                "        <footer>\n" +
                "            <a id=\"clear-completed\">Clear completed</a>\n" +
                "            <div id=\"todo-count\"></div>\n" +
                "        </footer>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"instructions\">\n" +
                "        Double-click to edit a todo.\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"credits\">\n" +
                "        Created by\n" +
                "        <br />\n" +
                "        <a href=\"https://github.com/liraz\">Liraz Shilkrot</a>.\n" +
                "        <br />Rewritten by: <a href=\"hhttps://github.com/jashkenas/backbone/blob/master/examples/todos\">Backbone.js Todos</a>.\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- Templates -->\n" +
                "    <script type=\"text/template\" id=\"item-template\">\n" +
                "        <div class=\"view\">\n" +
                "            <input class=\"toggle\" type=\"checkbox\" <%= done ? 'checked=\"checked\"' : '' %> />\n" +
                "            <label><%- title %></label>\n" +
                "            <a class=\"destroy\"></a>\n" +
                "        </div>\n" +
                "        <input class=\"edit\" type=\"text\" value=\"<%- title %>\" />\n" +
                "    </script>\n" +
                "\n" +
                "    <script type=\"text/template\" id=\"stats-template\">\n" +
                "        <% if (done) { %>\n" +
                "            <a id=\"clear-completed\">Clear <%= done %> completed <%= done == 1 ? 'item' : 'items' %></a>\n" +
                "        <% } %>\n" +
                "        <div class=\"todo-count\"><b><%= remaining %></b> <%= remaining == 1 ? 'item' : 'items' %> left</div>\n" +
                "    </script>";

        Document.get().getBody().setInnerHTML(html);
    }

    public void gwtTearDown() {

    }

    public void testTodosApp() {
        TodosEntryPoint todosEntryPoint = new TodosEntryPoint();
        todosEntryPoint.onModuleLoad();
    }
}
