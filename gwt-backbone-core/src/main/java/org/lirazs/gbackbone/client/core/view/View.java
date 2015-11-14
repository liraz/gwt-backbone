/*
 * Copyright 2015, Liraz Shilkrot
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.js.JsObject;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.util.UUID;

public abstract class View extends Events {

    private int id = -1;

    private String cid;
    private String tagName;
    private String className;

    private Model model;
    private Collection collection;
    private Element el;

    private Options attributes;
    private Properties events;

    private GQuery $el;

    RegExp delegateEventSplitter = RegExp.compile("/^(\\S+)\\s*(.*)$/");

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
    public View(JsObject options) {
        cid = UUID.uniqueId("view");

        if(options == null)
            options = JsObject.create("tagName", "div");

        if(options.exists("model"))
            model = options.get("model");
        if(options.exists("collection"))
            collection = options.get("collection");
        if(options.exists("el"))
            el = options.get("el");
        if(options.exists("id"))
            id = options.getInt("id");
        if(options.exists("attributes"))
            attributes = options.get("attributes");
        if(options.exists("className"))
            className = options.getString("className");
        if(options.exists("tagName"))
            tagName = options.getString("tagName");
        if(options.exists("events"))
            events = options.get("events");

        ensureElement();

        events = events();
        initialize(options);
        delegateEvents();
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

    protected abstract Properties events();

    /**
     * // Initialize is an empty function by default. Override it with your own
     // initialization logic.
     initialize(...args): void { }
     */
    protected abstract void initialize(JsObject options);

    /**
     * // **render** is the core function that your view should override, in order
     // to populate its element (`this.el`), with the appropriate HTML. The
     // convention is for **render** to always return `this`.
     render(): View {
        return this;
     }
     */
    protected abstract View render();

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
        this.$el.remove();
        this.stopListening();

        return this;
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

    /**
     * // Set callbacks, where `this.events` is a hash of
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
     // This only works for delegate-able events: not `focus`, `blur`, and
     // not `change`, `submit`, and `reset` in Internet Explorer.

     delegateEvents(events?: any): View {
         if (!(events || (events = _.result(this, 'events')))) return this;
         this.undelegateEvents();

         for (var key in events) {
             var method = events[key];

             if (!_.isFunction(method)) method = this[events[key]];
             if (!method) continue;

             var match = key.match(delegateEventSplitter);
             var eventName = match[1], selector = match[2];

             method = _.bind(method, this);
             eventName += '.delegateEvents' + this.cid;

             if (selector === '') {
                this.$el.on(eventName, method);
             } else {
                this.$el.on(eventName, selector, method);
             }
         }
         return this;
     }
     */

    protected View delegateEvents() {
        undelegateEvents();

        String[] keys = events.keys();
        for (String key : keys) {
            Function callback = events.getFunction(key);

            if(callback != null) {
                MatchResult matchResult = delegateEventSplitter.exec(key);
                String eventName = matchResult.getGroup(1);
                String selector = matchResult.getGroup(2);

                eventName += ".delegateEvents" + this.cid;

                if(selector.isEmpty()) {
                    this.$el.on(eventName, callback);
                } else {
                    this.$el.on(eventName, selector, callback);
                }
            }
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
    protected View undelegateEvents() {
        this.$el.off(".delegateEvents" + this.cid);
        return this;
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
        if(this.el == null && this.$el == null) {
            Options attrs = new Options().extend(attributes);

            if(this.id != -1)
                attrs.put("id", this.id);
            if(this.className != null)
                attrs.put("className", this.className);

            GQuery $el = GQuery.$("<" + this.tagName + ">").attr(attrs.toProperties());
            setElement($el, false);
        } else if(this.$el != null) {
            setElement(this.$el, false);
        } else {
            setElement(this.el, false);
        }
    }
}
