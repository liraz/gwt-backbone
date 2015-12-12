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
package org.lirazs.gbackbone.client.core.event;


import com.google.gwt.query.client.Function;
import com.google.gwt.regexp.shared.RegExp;
import org.lirazs.gbackbone.client.core.function.OnceFunction;
import org.lirazs.gbackbone.client.core.util.ArrayUtils;
import org.lirazs.gbackbone.client.core.util.UUID;

import java.util.*;

public class Events<T extends Events<T>> {

    String listenerId = UUID.uniqueId("l");

    Map<String, ListeningEntry> listeners = new HashMap<String, ListeningEntry>();
    Map<String, ListeningEntry> listeningTo = new HashMap<String, ListeningEntry>();
    Map<String, List<EventEntry>> events = new HashMap<String, List<EventEntry>>();

    RegExp eventsSplitter = RegExp.compile("\\s+");

    class EventEntry {
        private final Function callback;
        private final Object context;
        private final Object ctx;
        private final ListeningEntry listening;

        EventEntry(Function callback, Object context, Object ctx, ListeningEntry listening) {
            this.callback = callback;
            this.context = context;
            this.ctx = ctx;
            this.listening = listening;
        }
    }

    //{obj: obj, objId: id, id: thisId, listeningTo: listeningTo, count: 0};
    class ListeningEntry {
        private final Events obj;
        private final String objId;
        private final String id;
        private final Map<String, ListeningEntry> listeningTo;
        private int count;

        ListeningEntry(Events obj, String objId, String id, Map<String,
                        ListeningEntry> listeningTo) {
            this.obj = obj;
            this.objId = objId;
            this.id = id;
            this.listeningTo = listeningTo;
        }
        int incrementCount() {
            count++;
            return count;
        }
        int decreaseCount() {
            count--;
            return count;
        }
    }

    public int getListeningToCount() {
        return listeningTo.size();
    }
    public int getListenersCount() {
        return listeners.size();
    }
    public int getEventCount(String eventName) {
        List<EventEntry> eventEntries = events.get(eventName);
        return eventEntries != null ? eventEntries.size() : 0;
    }

    /** // Bind an event to a `callback` function. Passing `"all"` will bind
    // the callback to all events fired.
    Events.on = function(name, callback, context) {
        return internalOn(this, name, callback, context);
    };

    // Guard the `listening` argument from the public API.
    var internalOn = function(obj, name, callback, context, listening) {
        obj._events = eventsApi(onApi, obj._events || {}, name, callback, {
                context: context,
                ctx: obj,
                listening: listening
        });

        if (listening) {
            var listeners = obj._listeners || (obj._listeners = {});
            listeners[listening.id] = listening;
        }

        return obj;
    };*/

    /** // The reducing API that adds a callback to the `events` object.
    var onApi = function(events, name, callback, options) {
        if (callback) {
            var handlers = events[name] || (events[name] = []);
            var context = options.context, ctx = options.ctx, listening = options.listening;
            if (listening) listening.count++;

            handlers.push({ callback: callback, context: context, ctx: context || ctx, listening: listening });
        }
        return events;
    };*/

    public T on(Map<String, Function> events) {
        for (Map.Entry<String, Function> functionEntry : events.entrySet()) {
            Function function = functionEntry.getValue();
            if(function != null) {
                on(functionEntry.getKey(), function);
            }
        }

        return (T)this;
    }
    public T on(String name, Function callback) {
        return on(name, callback, null);
    }
    public T on(String name, Function callback, Object context) {
        internalOn(this, name, callback, context, null);
        return (T)this;
    }

    private void internalOn(Events obj, String name, Function callback, Object context, ListeningEntry listening) {
        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                internalOn(obj, splittedName, callback, context, listening);
            }
        } else {
            if (callback != null) {
                List<EventEntry> handlers = (List<EventEntry>) obj.events.get(name);
                if(handlers == null) {
                    handlers = new ArrayList<EventEntry>();
                    obj.events.put(name, handlers);
                }

                EventEntry eventEntry = new EventEntry(callback, context, context != null ? context : this, listening);
                handlers.add(eventEntry);
            }

            if(listening != null) {
                listening.incrementCount();
                obj.listeners.put(listening.id, listening);
            }
        }
    }

    /**
     *
     once(name: string, callback: (...args: any[]) => void , context?: any): any {
         if (!eventsApi(this, 'once', name, [callback, context]) || !callback) return this;
         var self = this;
         var once = _.once(function () {
             self.off(name, once);
             callback.apply(this, arguments);
         });
         once._callback = callback;
         return this.on(name, once, context);
     }
     */
    public T once(final String name, final Function callback) {
        return once(name, callback, null);
    }
    public T once(final String name, final Function callback, Object context) {
        if(callback == null) return (T)this;

        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                once(splittedName, callback, context);
            }
        } else {
            final OnceFunction onceCallback = new OnceFunction(name, callback) {
                @Override
                public void once() {
                    off(getName(), this);
                    getCallback().f(getArguments());
                }
            };
            on(name, onceCallback, context);
        }

        return (T)this;
    }

    public T once(Map<String, Function> events) {
        return once(events, null);
    }

    public T once(Map<String, Function> events, Object context) {
        for (Map.Entry<String, Function> functionEntry : events.entrySet()) {
            Function function = functionEntry.getValue();
            if(function != null) {
                once(functionEntry.getKey(), function, context);
            }
        }

        return (T)this;
    }

    /** // Remove one or many callbacks. If `context` is null, removes all
    // callbacks with that function. If `callback` is null, removes all
    // callbacks for the event. If `name` is null, removes all bound
    // callbacks for all events.
    Events.off =  function(name, callback, context) {
        if (!this._events) return this;
        this._events = eventsApi(offApi, this._events, name, callback, {
                context: context,
                listeners: this._listeners
        });
        return this;
    };*/

    /** // The reducing API that removes a callback from the `events` object.
    var offApi = function(events, name, callback, options) {
        if (!events) return;

        var i = 0, listening;
        var context = options.context, listeners = options.listeners;

        // Delete all events listeners and "drop" events.
        if (!name && !callback && !context) {
            var ids = _.keys(listeners);
            for (; i < ids.length; i++) {
                listening = listeners[ids[i]];
                delete listeners[listening.id];
                delete listening.listeningTo[listening.objId];
            }
            return;
        }

        var names = name ? [name] : _.keys(events);
        for (; i < names.length; i++) {
            name = names[i];
            var handlers = events[name];

            // Bail out if there are no events stored.
            if (!handlers) break;

            // Replace events if there are any remaining.  Otherwise, clean up.
            var remaining = [];
            for (var j = 0; j < handlers.length; j++) {
                var handler = handlers[j];
                if (
                        callback && callback !== handler.callback &&
                                callback !== handler.callback._callback ||
                                context && context !== handler.context
                        ) {
                    remaining.push(handler);
                } else {
                    listening = handler.listening;
                    if (listening && --listening.count === 0) {
                        delete listeners[listening.id];
                        delete listening.listeningTo[listening.objId];
                    }
                }
            }

            // Update tail event if the list has any events.  Otherwise, clean up.
            if (remaining.length) {
                events[name] = remaining;
            } else {
                delete events[name];
            }
        }
        if (_.size(events)) return events;
    };*/

    public T off() {
        return off(null, null, null);
    }

    public T off(Map<String, Function> events) {
        for (Map.Entry<String, Function> functionEntry : events.entrySet()) {
            Function function = functionEntry.getValue();
            if(function != null) {
                off(functionEntry.getKey(), function);
            }
        }

        return (T)this;
    }

    public T off(Object context) {
        return off(null, null, context);
    }
    public T off(Function callback) {
        return off(null, callback, null);
    }

    public T off(Function callback, Object context) {
        return off(null, callback, context);
    }

    public T off(String name) {
        return off(name, null, null);
    }
    public T off(String name, Function callback) {
        return off(name, callback, null);
    }

    public T off(String name, Function callback, Object context) {
        internalOff(name, callback, context, listeners);
        return (T)this;
    }

    private void internalOff(String name, Function callback, Object context,
                             Map<String, ListeningEntry> listeners) {

        if(events == null || events.isEmpty()) return;

        // Delete all events listeners and "drop" events.
        if(name == null && callback == null && context == null) {
            Set<String> ids = new HashSet<String>(listeners.keySet());
            for (String id : ids) {
                ListeningEntry listening = listeners.get(id);

                listeners.remove(listening.id);
                listening.listeningTo.remove(listening.objId);
            }

            this.events = new HashMap<String, List<EventEntry>>();
            return;
        }

        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                internalOff(splittedName, callback, context, listeners);
            }
        } else {
            // Replace events if there are any remaining.  Otherwise, clean up.
            String[] names;
            if(name != null) {
                names = new String[] {name};
            } else {
                names = events.keySet().toArray(new String[events.size()]);
            }

            for (String eventName : names) {
                if(events.containsKey(eventName)) {
                    List<EventEntry> handlers = events.get(eventName);

                    List<EventEntry> remaining = new ArrayList<EventEntry>();
                    events.put(eventName, remaining);

                    if(handlers.size() > 0) {

                        for (int i = 0; i < handlers.size(); i++) {
                            EventEntry handler = handlers.get(i);

                            if(callback != null && callback instanceof OnceFunction) { // make sure the original instance is used for checks
                                callback = ((OnceFunction)callback).getCallback();
                            }

                            Function eventEntryCallback = handler.callback;
                            if(eventEntryCallback != null && eventEntryCallback instanceof OnceFunction) { // make sure the original instance is used for checks
                                eventEntryCallback = ((OnceFunction)eventEntryCallback).getCallback();
                            }

                            if((callback != null && !callback.equals(eventEntryCallback))
                                    || (context != null && !context.equals(handler.context))) {
                                remaining.add(handler);
                            } else {

                                ListeningEntry listening = handler.listening;
                                if(listening != null && listening.decreaseCount() == 0) {
                                    listeners.remove(listening.id);
                                    listening.listeningTo.remove(listening.objId);
                                }
                            }
                        }
                    }

                    if(remaining.size() == 0) {
                        handlers.clear();
                        events.remove(eventName);
                    }
                }
            }
        }
    }

    /** // Trigger one or many events, firing all bound callbacks. Callbacks are
    // passed the same arguments as `trigger` is, apart from the event name
    // (unless you're listening on `"all"`, which will cause your callback to
    // receive the true name of the event as the first argument).
    Events.trigger =  function(name) {
        if (!this._events) return this;

        var length = Math.max(0, arguments.length - 1);
        var args = Array(length);
        for (var i = 0; i < length; i++) args[i] = arguments[i + 1];

        eventsApi(triggerApi, this._events, name, void 0, args);
        return this;
    };*/

    /** // Handles triggering the appropriate event callbacks.
    var triggerApi = function(objEvents, name, cb, args) {
        if (objEvents) {
            var events = objEvents[name];
            var allEvents = objEvents.all;
            if (events && allEvents) allEvents = allEvents.slice();
            if (events) triggerEvents(events, args);
            if (allEvents) triggerEvents(allEvents, [name].concat(args));
        }
        return objEvents;
    };*/

    public T trigger(String name, Object ...args) {
        if(events == null || events.isEmpty())
            return (T)this;

        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                trigger(splittedName, args);
            }
        } else {
            List<EventEntry> eventEntries = events.get(name);
            // support for all events listeners
            List<EventEntry> allEventEntries = events.get("all");

            if(eventEntries != null && eventEntries.size() > 0 &&
                    allEventEntries != null && allEventEntries.size() > 0) {
                // make sure provided all events array cannot grow/shrink while in trigger
                allEventEntries = new ArrayList<EventEntry>(allEventEntries);
            }
            if(eventEntries != null && eventEntries.size() > 0) {
                triggerEvents(eventEntries, args);
            }
            if(allEventEntries != null && allEventEntries.size() > 0) {
                triggerEvents(allEventEntries, ArrayUtils.joinArrays(new Object[] {name}, args));
            }
        }
        return (T)this;
    }

    /** // Tell this object to stop listening to either specific events ... or
    // to every object it's currently listening to.
    Events.stopListening =  function(obj, name, callback) {
        var listeningTo = this._listeningTo;
        if (!listeningTo) return this;

        var ids = obj ? [obj._listenId] : _.keys(listeningTo);

        for (var i = 0; i < ids.length; i++) {
            var listening = listeningTo[ids[i]];

            // If listening doesn't exist, this object is not currently
            // listening to obj. Break out early.
            if (!listening) break;

            listening.obj.off(name, callback, this);
        }
        if (_.isEmpty(listeningTo)) this._listeningTo = void 0;

        return this;
    };*/
    public T stopListening() {
        return stopListening(null, null, null);
    }
    public T stopListening(String name) {
        return stopListening(null, name, null);
    }
    public T stopListening(Events obj) {
        return stopListening(obj, null, null);
    }
    public T stopListening(Events obj, String name) {
        return stopListening(obj, name, null);
    }

    public T stopListening(final Events obj, String name, Function callback) {
        Map<String, ListeningEntry> listeningTo = this.listeningTo;
        if(listeningTo == null || listeningTo.isEmpty()) return (T)this;

        Set<String> ids = obj != null ? new HashSet<String>(Collections.singletonList(obj.listenerId))
                : new HashSet<String>(listeningTo.keySet());

        for (String id : ids) {
            ListeningEntry listening = listeningTo.get(id);

            // If listening doesn't exist, this object is not currently
            // listening to obj. Break out early.
            if(listening == null) break;

            listening.obj.off(name, callback, this);
        }
        if(listeningTo.isEmpty()) this.listeningTo = new HashMap<String, ListeningEntry>();

        return (T)this;
    }

    public T stopListening(Events obj, Map<String, Function> events) {
        for (Map.Entry<String, Function> functionEntry : events.entrySet()) {
            Function function = functionEntry.getValue();
            if(function != null) {
                stopListening(obj, functionEntry.getKey(), function);
            }
        }

        return (T)this;
    }

    /** // Inversion-of-control versions of `on`. Tell *this* object to listen to
    // an event in another object... keeping track of what it's listening to
    // for easier unbinding later.
    Events.listenTo =  function(obj, name, callback) {
        if (!obj) return this;
        var id = obj._listenId || (obj._listenId = _.uniqueId('l'));
        var listeningTo = this._listeningTo || (this._listeningTo = {});
        var listening = listeningTo[id];

        // This object is not listening to any other events on `obj` yet.
        // Setup the necessary references to track the listening callbacks.
        if (!listening) {
            var thisId = this._listenId || (this._listenId = _.uniqueId('l'));
            listening = listeningTo[id] = {obj: obj, objId: id, id: thisId, listeningTo: listeningTo, count: 0};
        }

        // Bind callbacks on obj, and keep track of them on listening.
        internalOn(obj, name, callback, this, listening);
        return this;
    };*/

    public T listenTo(Events obj, Map<String, Function> events) {
        for (Map.Entry<String, Function> functionEntry : events.entrySet()) {
            Function function = functionEntry.getValue();
            if(function != null) {
                listenTo(obj, functionEntry.getKey(), function);
            }
        }

        return (T)this;
    }

    public T listenTo(Events obj, String name, Function callback) {
        if(obj == null) return (T)this;

        String id = obj.listenerId;
        Map<String, ListeningEntry> listeningTo = this.listeningTo;
        ListeningEntry listening = listeningTo.get(id);

        // This object is not listening to any other events on `obj` yet.
        // Setup the necessary references to track the listening callbacks.
        if(listening == null) {
            String thisId = this.listenerId;
            ListeningEntry listeningEntry = new ListeningEntry(
                    obj, id, thisId, listeningTo
            );
            listeningTo.put(id, listeningEntry);
            listening = listeningEntry;
        }

        // Bind callbacks on obj, and keep track of them on listening.
        internalOn(obj, name, callback, this, listening);

        return (T)this;
    }


    public T listenToOnce(Events obj, Map<String, Function> events) {
        for (Map.Entry<String, Function> functionEntry : events.entrySet()) {
            Function function = functionEntry.getValue();
            if(function != null) {
                listenToOnce(obj, functionEntry.getKey(), function);
            }
        }

        return (T)this;
    }

    public T listenToOnce(final Events obj, String name, Function callback) {
        if(callback == null) return (T)this;

        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                listenToOnce(obj, splittedName, callback);
            }
        } else {
            final OnceFunction onceCallback = new OnceFunction(name, callback) {
                @Override
                public void once() {
                    stopListening(obj, getName(), this);
                    getCallback().f(getArguments());
                }
            };
            listenTo(obj, name, onceCallback);
        }

        return (T)this;
    }

    /**
     * var triggerEvents = function (events, args) {
         var ev, i = -1, l = events.length, a1 = args[0], a2 = args[1], a3 = args[2];
         switch (args.length) {
         case 0: while (++i < l) (ev = events[i]).callback.call(ev.ctx); return;
         case 1: while (++i < l) (ev = events[i]).callback.call(ev.ctx, a1); return;
         case 2: while (++i < l) (ev = events[i]).callback.call(ev.ctx, a1, a2); return;
         case 3: while (++i < l) (ev = events[i]).callback.call(ev.ctx, a1, a2, a3); return;
         default: while (++i < l) (ev = events[i]).callback.apply(ev.ctx, args);
         }
     };
     */
    private void triggerEvents(List<EventEntry> events, Object ...args) {
        int i = -1;
        int length = events.size();

        while(++i < length) {
            EventEntry event = events.get(i);
            event.callback.f(args);
        }
    }

    // Binding Aliases for backwards compatibility.
    public T unbind() {
        return unbind(null, null, null);
    }

    public T unbind(Map<String, Function> events) {
        return off(events);
    }

    public T unbind(Object context) {
        return unbind(null, null, context);
    }
    public T unbind(Function callback) {
        return unbind(null, callback, null);
    }

    public T unbind(Function callback, Object context) {
        return unbind(null, callback, context);
    }

    public T unbind(String name) {
        return unbind(name, null, null);
    }
    public T unbind(String name, Function callback) {
        return unbind(name, callback, null);
    }

    public T unbind(String name, Function callback, Object context) {
        return off(name, callback, context);
    }


    public T bind(Map<String, Function> events) {
        return on(events);
    }
    public T bind(String name, Function callback) {
        return on(name, callback, null);
    }
    public T bind(String name, Function callback, Object context) {
        return on(name, callback, context);
    }
}
