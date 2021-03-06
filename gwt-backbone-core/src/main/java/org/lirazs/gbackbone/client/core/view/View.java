/*
 * Copyright 2016, Liraz Shilkrot
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.lirazs.gbackbone.client.core.view;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.Properties;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event;
import org.lirazs.gbackbone.client.core.annotation.*;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.util.UUID;
import org.lirazs.gbackbone.reflection.client.*;

import java.util.HashMap;
import java.util.Map;

@Reflectable(classAnnotations = true, fields = true, methods = true, constructors = false,
        fieldAnnotations = true, relationTypes=false,
        superClasses=true, assignableClasses=false)
public class View<M extends Model> extends Events<View<M>> {
    private String id;

    private String cid;
    private String tagName;
    private String className;

    private M model;
    private Collection collection;
    private Element el;
    private String elSelector;
    private GQuery elGQuerySelector;

    private Options attributes;
    private Properties events;
    private Map<String, ViewEventEntry> delegatedEvents = new HashMap<String, ViewEventEntry>();

    private GQuery $el;
    private Template template;

    RegExp delegateEventSplitter = RegExp.compile("^(\\S+)\\s*(.*)$");

    /**
     * // List of view options to be merged as properties.
     var viewOptions = ['model', 'collection', 'el', 'id', 'attributes', 'className', 'tagName', 'events'];
     *
     * constructor(options?: ViewOptions) {
         super();

         if (!this.tagName)
            this.tagName = 'div';

         this.cid = _.uniqueId('view');

         options || (options = {});
         _.extend(this, _.pick(options, viewOptions));

         this._ensureElement();

         this.initialize.apply(this, arguments);
         this.delegateEvents();
     }
     */
    public View() {
        this(null);
    }
    public View(Options options) {
        cid = UUID.uniqueId("view");

        if(options == null)
            options = new Options();

        options.defaults(new Options("tagName", "div"));

        if(options.containsKey("model"))
            model = options.get("model");
        if(options.containsKey("collection"))
            collection = options.get("collection");
        if(options.containsKey("el")) {
            Object el = options.get("el");
            if(el instanceof String)
                this.elSelector = (String) el;
            else if(el instanceof GQuery) {
                this.elGQuerySelector = (GQuery) el;
            }
            else
                this.el = (Element) el;
        }
        if(options.containsKey("id"))
            id = options.get("id");
        if(options.containsKey("attributes"))
            attributes = options.get("attributes");
        if(options.containsKey("className"))
            className = options.get("className");
        if(options.containsKey("tagName"))
            tagName = options.get("tagName");
        if(options.containsKey("events"))
            events = options.get("events");

        // start model injections only if there's model or attributes available
        if(attributes != null || model != null) {
            bindAnnotatedModelInjections();
        }

        bindAnnotatedTagName();
        bindAnnotatedElement();

        ensureElement();
        bindAnnotatedViewInjections();
        bindAnnotatedEvents();

        initialize();
        initialize(options);

        bindAnnotatedTemplate();
        delegateEvents();

        // if a template is being loaded
        if(template == null && hasTemplateAnnotationWithFilePath()) {
            listenToOnce(this, "template:complete", new Function() {
                @Override
                public void f() {
                    bindAnnotatedViewInjections();
                }
            });
        }
    }

    private void bindAnnotatedTagName() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());
            ViewTagName annotation = classType.getAnnotation(ViewTagName.class);

            if(annotation != null && classType.isClass() != null) {
                String tagNameValue = annotation.value();

                if(tagNameValue != null && !tagNameValue.isEmpty()) { // rendering the template value as is
                    tagName = tagNameValue;
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    private void bindAnnotatedElement() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());
            ViewElement annotation = classType.getAnnotation(ViewElement.class);

            if(annotation != null && classType.isClass() != null) {
                String elementValue = annotation.value();

                if(elementValue != null && !elementValue.isEmpty()) { // rendering the template value as is
                    elSelector = elementValue;
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    private void bindAnnotatedEvents() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());

            Method[] methods = classType.getMethods();
            for (final Method method : methods) {
                EventHandler annotation = method.getAnnotation(EventHandler.class);
                if(annotation != null && method.isPublic()) {

                    String value = annotation.value();
                    if(!value.isEmpty()) {
                        final View thisView = this;
                        MatchResult matchResult = delegateEventSplitter.exec(value);
                        delegate(matchResult.getGroup(1), matchResult.getGroup(2), new Function() {
                            @Override
                            public void f() {
                                Event event = getEvent();
                                Element element = getElement();

                                Parameter[] parameters = method.getParameters();
                                Object[] args = new Object[parameters.length];

                                for (int i = 0; i < parameters.length; i++) {
                                    Parameter parameter = parameters[i];
                                    String typeName = parameter.getTypeName();

                                    if(typeName.equals("com.google.gwt.user.client.Event")) {
                                        args[i] = event;
                                    }
                                    if(typeName.equals("com.google.gwt.dom.client.Element")) {
                                        args[i] = element;
                                    }
                                }

                                method.invoke(thisView, args);
                            }
                        });
                    }
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    private void bindAnnotatedTemplate() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());
            ViewTemplate annotation = classType.getAnnotation(ViewTemplate.class);

            if(annotation != null && classType.isClass() != null) {
                String templateValue = annotation.value();
                String templateFilePath = annotation.filePath();
                String templateSelector = annotation.selector();
                final boolean autoRender = annotation.autoRender();
                final boolean callRenderWhenComplete = annotation.callRenderWhenComplete();

                if(templateValue != null && !templateValue.isEmpty()) { // rendering the template value as is
                    template = TemplateFactory.template(templateValue);
                    if(autoRender) {
                        Options attributes = getTemplateAttributes();
                        get$El().html(template.apply(attributes));
                    }
                    if(callRenderWhenComplete) {
                        render();
                    }

                } else if(templateSelector != null && !templateSelector.isEmpty()) { // rendering the template value as is
                    GQuery scriptElement = GQuery.$(templateSelector);
                    template = TemplateFactory.template(scriptElement.html());
                    if(autoRender) {
                        Options attributes = getTemplateAttributes();
                        get$El().html(template.apply(attributes));
                    }
                    if(callRenderWhenComplete) {
                        render();
                    }

                } else if(templateFilePath != null && !templateFilePath.isEmpty()) { // rendering the template async
                    trigger("template:load");

                    Promise promise = TemplateFactory.loadTemplate(templateFilePath);
                    promise.progress(new Function() {
                        @Override
                        public void f() {
                            int progress = getArgument(0);
                            trigger("template:progress", progress);
                        }
                    });
                    promise.done(new Function() {
                        @Override
                        public void f() {
                            template = getArgument(0);
                            if(autoRender) {
                                Options attrs = getTemplateAttributes();
                                get$El().html(template.apply(attrs));
                            }
                            trigger("template:complete");

                            if(callRenderWhenComplete) {
                                render();
                            }
                        }
                    });
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    private boolean hasTemplateAnnotationWithFilePath() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());
            ViewTemplate annotation = classType.getAnnotation(ViewTemplate.class);

            return annotation != null && annotation.filePath() != null && !annotation.filePath().isEmpty();
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
        return false;
    }

    /**
     * Can be used by a custom view rendering enviornment -
     * since View class cannot know when the developer is appending the markup
     */
    protected void injectViews() {
        bindAnnotatedViewInjections();
    }

    private void bindAnnotatedViewInjections() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());

            Field[] fields = classType.getFields();
            for (final Field field : fields) {
                InjectView annotation = field.getAnnotation(InjectView.class);
                if(annotation != null && field.isPublic()) {
                    GQuery $element = null;
                    String value = annotation.value();

                    if(!value.isEmpty()) { // using the value as selector
                        $element = $(value);
                    } else {
                        $element = $("#" + field.getName());
                    }

                    String elementClass = field.getTypeName();
                    if(elementClass != null) {
                        if(elementClass.equals("com.google.gwt.query.client.GQuery")) {
                            // we have a GQuery element field..
                            //field.setFieldValue(this, $element);
                            field.getEnclosingType().setFieldValue(this, field.getName(), $element);
                        } else {
                            // probably some other element type
                            //field.setFieldValue(this, $element.get(0));
                            field.getEnclosingType().setFieldValue(this, field.getName(), $element.get(0));
                        }
                    }
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    /**
     * Can be used by a custom view model appending enviornment -
     * since View class cannot know when the developer is deciding to attach the model into the view
     */
    protected void injectModels() {
        bindAnnotatedModelInjections();
    }

    private void bindAnnotatedModelInjections() {
        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());

            Field[] fields = classType.getFields();
            for (final Field field : fields) {
                InjectModel annotation = field.getAnnotation(InjectModel.class);
                if(annotation != null) {
                    String attributeName = null;
                    String value = annotation.value();

                    if(!value.isEmpty()) { // using the value as selector
                        attributeName = value;
                    } else {
                        attributeName = field.getName();
                    }

                    if(model != null) {
                        field.setFieldValue(this, model.get(attributeName));
                    } else if(attributes != null) {
                        field.setFieldValue(this, attributes.get(attributeName));
                    }
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    private Options getTemplateAttributes() {
        Options attrs = new Options();
        if(model != null) {
            attrs = model.getAttributes();
        } else if(attributes != null) {
            attrs = attributes;
        }
        return attrs;
    }

    protected void initialize() {
        // override
    }
    protected void initialize(Options options) {
        // override
    }

    public String getId() {
        return id;
    }

    public Options getAttributes() {
        return attributes;
    }

    public String getClassName() {
        return className;
    }

    public String getTagName() {
        return tagName;
    }

    public M getModel() {
        return model;
    }

    public Collection getCollection() {
        return collection;
    }

    public Template getTemplate() {
        return template;
    }

    /**
     * // jQuery delegate for element lookup, scoped to DOM elements within the
     // current view. This should be prefered to global lookups where possible.
     $(selector: string): JQuery {
        return this.$el.find(selector);
     }
     */
    public GQuery $(String selector) {
        return $el.find(selector);
    }

    protected Properties events() {
        return null;
    }

    /**
     * // **render** is the core function that your view should override, in order
     // to populate its element (`this.el`), with the appropriate HTML. The
     // convention is for **render** to always return `this`.
     render(): View {
        return this;
     }
     */
    public View render() {
        return this;
    }

    /**
     * // Remove this view by taking the element out of the DOM, and removing any
     // applicable Backbone.Events listeners.
     remove(): View {
         this.$el.remove();
         this.stopListening();

         return this;
     }
     */
    public View remove() {
        removeElement();
        stopListening();

        return this;
    }

    /** Remove this view's element from the document and all event listeners
    // attached to it. Exposed for subclasses using an alternative DOM
    // manipulation API.
    _removeElement: function() {
        this.$el.remove();
    },*/
    public void removeElement() {
        $el.remove();
    }

    public String getElSelector() {
        return elSelector;
    }

    public Element getEl() {
        return el;
    }

    public GQuery get$El() {
        return $el;
    }

    /**
     * // Change the view's element (`this.el` property), including event
     // re-delegation.
     setElement(element: any, delegate?: boolean): View {
         if (this.$el) this.undelegateEvents();
         this.$el = element instanceof Backbone.$ ? element : Backbone.$(element);
         this.el = this.$el[0];
         if (delegate !== false) this.delegateEvents();

         return this;
     }
     */
    public View setElement(String html) {
        return setElement(html, true);
    }
    public View setElement(String html, boolean delegate) {
        return setElement(GQuery.$(html), delegate);
    }
    public View setElement(GQuery element) {
        return setElement(element, true);
    }
    public View setElement(GQuery element, boolean delegate) {
        if($el != null)
            undelegateEvents();

        $el = element;
        el = element.get(0);

        if(delegate)
            delegateEvents();

        return this;
    }

    public View setElement(Element element) {
        return setElement(element, true);
    }
    public View setElement(Element element, boolean delegate) {
        if($el != null)
            undelegateEvents();

        $el = GQuery.$(element);
        el = element;

        if(delegate)
            delegateEvents();

        return this;
    }

    /** Set callbacks, where `this.events` is a hash of
    //
    // *{"event selector": "callback"}*
    //
    //     {
    //       'mousedown .title':  'edit',
    //       'click .button':     'save',
    //       'click .open':       function(e) { ... }
    //     }
    //
    // pairs. Callbacks will be bound to the view, with `this` set properly.
    // Uses event delegation for efficiency.
    // Omitting the selector binds the event to `this.el`.
    delegateEvents: function(events) {
        events || (events = _.result(this, 'events'));

        if (!events) return this;

        this.undelegateEvents();

        for (var key in events) {
            var method = events[key];

            if (!_.isFunction(method)) method = this[method];
            if (!method) continue;

            var match = key.match(delegateEventSplitter);
            this.delegate(match[1], match[2], _.bind(method, this));
        }
        return this;
    },*/

    public View delegateEvents() {
        return delegateEvents(null);
    }
    public View delegateEvents(Properties events) {
        if(events == null)
            events = this.events;
        if(events == null)
            events = events();

        if(events == null) // couldn't find events anywhere.. just return
            return this;

        undelegateEvents();

        String[] keys = events.keys();
        for (String key : keys) {
            Function callback = events.getFunction(key);

            if(callback != null) {
                MatchResult matchResult = delegateEventSplitter.exec(key);
                delegate(matchResult.getGroup(1), matchResult.getGroup(2), callback);
            }
        }
        return this;
    }

    /** Add a single event listener to the view's element (or a child element
    // using `selector`). This only works for delegate-able events: not `focus`,
    // `blur`, and not `change`, `submit`, and `reset` in Internet Explorer.
    delegate: function(eventName, selector, listener) {
        this.$el.on(eventName + '.delegateEvents' + this.cid, selector, listener);
        return this;
    },*/
    public View delegate(String eventName, Function listener) {
        return delegate(eventName, null, listener);
    }
    public View delegate(String eventName, String selector, Function listener) {
        eventName += ".delegateEvents" + this.cid;

        if(selector != null && !selector.isEmpty()) {
            this.$el.on(eventName, selector, listener);
            delegatedEvents.put(eventName, new ViewEventEntry(eventName, selector, listener));
        } else {
            this.$el.on(eventName, listener);
            delegatedEvents.put(eventName, new ViewEventEntry(eventName, listener));
        }
        return this;
    }

    /**
     * // Clears all callbacks previously bound to the view with `delegateEvents`.
     // You usually don't need to use this, but may wish to if you have multiple
     // Backbone views attached to the same DOM element.
     undelegateEvents(): View {
         this.$el.off('.delegateEvents' + this.cid);
         return this;
     }
     */
    public View undelegateEvents() {
        // this cannot work with gQuery..
        //this.$el.off(".delegateEvents" + this.cid);

        for (ViewEventEntry delegatedEvent : delegatedEvents.values()) {
            if(delegatedEvent.selector != null && !delegatedEvent.selector.isEmpty()) {
                this.$el.undelegate(delegatedEvent.selector, delegatedEvent.event);
            } else {
                this.$el.unbind(delegatedEvent.event, delegatedEvent.function);
            }
        }
        delegatedEvents.clear();

        return this;
    }

    /** A finer-grained `undelegateEvents` for removing a single delegated event.
    // `selector` and `listener` are both optional.
    undelegate: function(eventName, selector, listener) {
        this.$el.off(eventName + '.delegateEvents' + this.cid, selector, listener);
        return this;
    },*/
    public View undelegate() {
        return undelegate(null, null, null);
    }
    public View undelegate(String eventName) {
        return undelegate(eventName, null, null);
    }
    public View undelegate(String eventName, String selector) {
        return undelegate(eventName, selector, null);
    }
    public View undelegate(String eventName, Function listener) {
        return undelegate(eventName, null, listener);
    }
    public View undelegate(String eventName, String selector, Function listener) {
        if(eventName == null) {
            undelegateEvents();
        } else {
            eventName += ".delegateEvents" + this.cid;

            if(selector != null && !selector.isEmpty()) {
                this.$el.undelegate(selector, eventName);
            } else if(listener != null) {
                this.$el.unbind(eventName, listener);
            } else {
                ViewEventEntry viewEventEntry = delegatedEvents.get(eventName);

                this.$el.undelegate(viewEventEntry.selector, eventName);
                this.$el.unbind(eventName);
            }
            delegatedEvents.remove(eventName);
        }

        return this;
    }

    /** Produces a DOM element to be assigned to your view. Exposed for
    // subclasses using an alternative DOM manipulation API.
    _createElement: function(tagName) {
        return document.createElement(tagName);
    },*/
    public Element createElement(String tagName) {
        return Document.get().createElement(tagName);
    }

    /**
     * // Ensure that the View has a DOM element to render into.
     // If `this.el` is a string, pass it through `$()`, take the first
     // matching element, and re-assign it to `el`. Otherwise, create
     // an element from the `id`, `className` and `tagName` properties.
     _ensureElement(): void {
         if (!this.el) {
             var attrs = _.extend({}, _.result(this, 'attributes'));
             if (this.id) attrs.id = _.result(this, 'id');
             if (this.className) attrs['class'] = _.result(this, 'className');
             var $el = Backbone.$('<' + _.result(this, 'tagName') + '>').attr(attrs);

             this.setElement($el, false);
         } else {
            this.setElement(_.result(this, 'el'), false);
         }
     }
     */
    private void ensureElement() {
        if(this.el == null && this.getElSelector() == null && this.elGQuerySelector == null) {
            Options attrs = new Options().extend(getAttributes());

            if(getId() != null)
                attrs.put("id", getId());
            if(getClassName() != null)
                attrs.put("class", getClassName());

            GQuery $el = GQuery.$("<" + this.getTagName() + ">").attr(attrs.toProperties());
            setElement($el, false);
        } else if(this.getElSelector() != null) {
            setElement(this.getElSelector(), false);
        } else if(this.elGQuerySelector != null) {
            setElement(this.elGQuerySelector, false);
        } else {
            setElement(this.el, false);
        }
    }

    private class ViewEventEntry {
        private String event;
        private String selector;
        private Function function;

        public ViewEventEntry(String event, String selector, Function function) {
            this.event = event;
            this.selector = selector;
            this.function = function;
        }

        public ViewEventEntry(String eventName, Function callback) {
            this.event = eventName;
            this.function = callback;
        }
    }
}
