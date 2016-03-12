
GwtBackbone v0.5.0 [![Travis-CI Build Status](https://travis-ci.org/liraz/gwt-backbone.svg?branch=master)](https://travis-ci.org/liraz/gwt-backbone)
===========

GwtBackbone a.k.a. GBackbone is a Backbone-like API written in GWT, which allows GWT to be used convert already written enterprise backbone projects into entirely used in GWT.
That aim here is to provide the tools to those who don't have the time to change all of their project to some type safe language, and rewrite the whole thing.
With GBackbone you can use the same programming methodology currently written & used in your company, and just convert it into type safe, compile time syntax checking & way more organized java project.

GBackbone is using the same API currently available in backbone, and will continue to do so until imitating the full capabilities of backbone.
The library is completely dependant on the *GQuery a.k.a GwtQuery project*, and will take it as one of it's dependencies.
Currently what mostly is used from GwtQuery is JsMap, Function & the whole selector, events engines available within the framework (like Backbone is using jQuery/Zepto etc...).

*Gwt version used is 2.7.0*
All functionality is available and tested, in addition to some utility annotations and helpers that can be used.


Demo
=======
Check out the demo source here (Todos application):
https://github.com/liraz/gwt-backbone/tree/master/gwt-backbone-examples/gwt-backbone-examples-todos

Use the following link to see the Todos application live running:
https://gbackbone-1198.appspot.com/

Demo was tested on IE 8,9,10+, FireFox, Chrome, Android & iPhone

Highlights
=======

Router @Route annotations
------
@Route can be declared with multiple routes as array of string values.
Arguments can be with the same name as what is passed into the route regex.
If specifying String[] router will give you all the arguments that were provided for the route.

```java
public class AnnotatedRouter extends Router {
    @Override
    protected void initialize(Options options) {
    }

    @Route(value = {"counter", "counter2"})
    public void counter() {
        count++;
    }

    @Route("search/:query")
    public void search(String query) {
        this.query = query;
    }

    @Route("search/:query/p:page")
    public void searchArgs(String[] args) {
        this.query = args[0];
        this.page = args[1];
    }
}
```

View @EventHandler annotations for attaching events
------
@EventHandler can be declared with an event selector like you would do the same with events object in Backbone.js.
If specifying Event or Element, those objects will be injected automatically.

```java
public class AnnotatedView extends View {
    int[] counter;
    public AnnotatedView(Options options, int[] counter) {
        super(options);
        this.counter = counter;
    }

    @EventHandler("click h1")
    public void foo(Event event, Element element) {
        counter[0]++;

        assert event != null;
        assert element != null;
    }

    @EventHandler("click")
    public void bar(Element element, Event event) {
        counter[0]++;

        assert event != null;
        assert element != null;
    }

    @EventHandler("click")
    public void noArgs() {
        counter[0]++;
    }
}
```

View @ViewTemplate annotations for attaching a template for the view
------
If "filePath" is provided, the template will be loaded on demand, a "template:load" event will be dispatched,
then when completed loading an event of "template:complete" will be dispatched once the template was loaded.
If a default "value" is provided, it'll be considered as a template string.
```java
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class AnnotatedModelInjectedView extends View {
    @InjectView
    public InputElement emailInput;

    public AnnotatedModelInjectedView(Options options) {
        super(options);
    }

    @Override
    public View render() {
        emailInput.setValue("My Email!");

        return this;
    }
}
```

```java
@ViewTemplate("<%= value %>")
public class AnnotatedTemplateView extends View {

    public AnnotatedTemplateView(Options options) {
        super(options);
    }
}
```

Also a selector of the <script id="item-template"></script> element holding the html template currently in the document can be provided:
```java
@ViewTemplate(selector = "#item-template")
public class ListItemView extends View {

    public ListItemView(Options options) {
        super(options);
    }
}
```

View @InjectView annotation for injecting views that exists inside the $el
------
A GWT element can be injected, as well GQuery is supported.
The annotation can be provided with a selector to get the element injected.
Or you can also leave it empty, then the property name will be taken in the following selector pattern "#%s".
```java
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class InjectedView extends View {

    @InjectView
    public InputElement emailInput;

    @InjectView("#emailRetypeSpecialId")
    public InputElement emailRetypeInput;

    @InjectView(".phoneInputClass")
    public InputElement phoneNumberInput;

    @InjectView(".addressInputAsGQuery")
    public GQuery addressInput;

    @InjectView
    public GQuery cityInput;

    public AnnotatedModelInjectedView(Options options) {
        super(options);
    }

    @Override
    public View render() {
        emailInput.setValue("value@value.com");

        return this;
    }
}
```

View @InjectModel annotation for injecting properties from model
------
```java
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class AnnotatedModelInjectedView extends View {

    @InjectModel
    public String email; // injected from model, field name is taken as property name

    @InjectView
    public InputElement emailInput;

    public AnnotatedModelInjectedView(Options options) {
        super(options);
    }

    @Override
    public View render() {
        emailInput.setValue(email);

        return this;
    }
}
```

View @ViewTagName annotation for specifying tag name for a view
------
```java
@ViewTagName("li")
public class TodoView extends View<Todo> {

}
```

View @ViewElement annotation for specifying the view's element that is in the DOM
------
```java
@ViewElement("#todoapp")
public class AppView extends View {

}
```


Adding to your project
=======

For now this package is available only via https://jitpack.io/

### Gradle dependencies
```
repositories {
  maven {
    url "https://jitpack.io"
  }
}

dependencies {
    compile 'com.github.liraz.gwt-backbone:gwt-backbone:0.4.1'
}
```

### Maven dependencies
```
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.liraz.gwt-backbone</groupId>
    <artifactId>gwt-backbone</artifactId>
    <version>0.4.1</version>
</dependency>
```


Usage
=======

Collection
------
```java
import static org.lirazs.gbackbone.client.core.data.Options.O;
import static org.lirazs.gbackbone.client.core.data.OptionsList.OL;

final Collection<Model> col = new Collection<Model>();
col.add(OL(
        O("id", 0, "name", "one"),
        O("id", 1, "name", "two")
));

Model one = col.get(0);
assertEquals(one.get("name"), "one");

col.on("change:name", new Function() {
    @Override
    public void f() {
        Model model = getArgument(0);
        assertNotNull(col.get(model));
    }
});
one.set(new Options("name", "dalmatians", "id", 101));
```

Model
------
```java
import static org.lirazs.gbackbone.client.core.data.Options.O;

JSONObject a = new JSONObject();
a.put("_id", new JSONNumber(100));

MongoModel model = new MongoModel(a);
Collection<MongoModel> col = new Collection<MongoModel>(MongoModel.class, model);

Model a = new Model(O("id", 3, "label", "a"));
Model b = new Model(O("id", 2, "label", "b"));
Model c = new Model(O("id", 1, "label", "c"));
Model d = new Model(O("id", 0, "label", "d"));
```

Router
------
```java
public class TestRouter extends Router {
    @Override
    protected Map<String, ?> routes() {
        Map<String, Object> routes = new LinkedHashMap<String, Object>();

        routes.put("query/:entity", "query");
        routes.put("function/:value", new Function() {
            @Override
            public void f() {
                value = getArgument(0);
            }
        });
        routes.put("*anything", "anything");

        return routes;
    }

    @Override
    protected void initialize(Options options) {
    }

    public void query(String entity, String args) {
        this.entity = entity;
        this.queryArgs = args;
    }

    public void anything(String whatever) {
        this.anything = whatever;
    }
}

History.get().setInterval(9);
History.get().start(O(
    "pushState", false
));
```

Events
------
```java
Events a = new Events();
Events b = new Events();

final Function fn = new Function() {
    @Override
    public void f() {
        counter[0]++;
    }
};

a.once("event", fn);
b.on("event", fn);

a.trigger("event");
b.trigger("event");
```

View
------
```java
import static org.lirazs.gbackbone.client.core.data.Options.O;

View view = new View(O("el", "#testElement"));
view.render();

View view = new CustomView(O(
        "model", new Model(),
        "collection", new Collection<Model>()
));

view.stopListening();
view.getModel().trigger("x");
view.getCollection().trigger("x");
```

