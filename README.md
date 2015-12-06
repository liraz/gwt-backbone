
GwtBackbone v0.2.1
===========

GwtBackbone a.k.a. GBackbone is a Backbone-like API written in GWT, which allows GWT to be used convert already written enterprise backbone projects into entirely used in GWT.
That aim here is to provide the tools to those who don't have the time to change all of their project to some type safe language, and rewrite the whole thing.
With GBackbone you can use the same programming methodology currently written & used in your company, and just convert it into type safe, compile time syntax checking & way more organized java project.

GBackbone is using the same API currently available in backbone, and will continue to do so until imitating the full capabilities of backbone.
The library is completely dependant on the *GQuery a.k.a GwtQuery project*, and will take it as one of it's dependencies.
Currently what mostly is used from GwtQuery is JsMap, Function & the whole selector, events engines available within the framework (like Backbone is using jQuery/Zepto etc...).

*Gwt version used is 2.7.0*, but will be made available for earlier versions in the future.
First all tests should be completely written, then moving on to backward compatibility.

Currently, not all functionality is supported. Library is in a very early stage.
Mostly base functionality like Collection, Model & View are available, but still missing a lot more features to actually make this usable to convert an entire application.
As well currently only tests for the Collection are available - and not yet fully tested.

As i go on, i'll create a more organized list of all features still missing.


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
    compile 'com.github.liraz:gwt-backbone:0.2.1'
}
```

### Maven dependencies
```
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.liraz</groupId>
    <artifactId>gwt-backbone</artifactId>
    <version>0.2.1</version>
</dependency>
```


Usage
=======

Collection
------
```java
final Collection<Model> col = new Collection<Model>();
col.add(new OptionsList(
        new Options("id", 0, "name", "one"),
        new Options("id", 1, "name", "two")
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
JSONObject a = new JSONObject();
a.put("_id", new JSONNumber(100));

MongoModel model = new MongoModel(a);
Collection<MongoModel> col = new Collection<MongoModel>(MongoModel.class, model);

Model a = new Model(new Options("id", 3, "label", "a"));
Model b = new Model(new Options("id", 2, "label", "b"));
Model c = new Model(new Options("id", 1, "label", "c"));
Model d = new Model(new Options("id", 0, "label", "d"));
```
