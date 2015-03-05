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
import org.lirazs.gbackbone.client.core.util.ArrayUtils;
import org.lirazs.gbackbone.client.core.util.UUID;

import java.util.*;

public class Events {

    String listenerId = UUID.uuid();

    Map<String, Events> listeners = new HashMap<String, Events>();
    Map<String, List<EventEntry>> events = new HashMap<String, List<EventEntry>>();

    RegExp eventsSplitter = RegExp.compile("/\\s+/");

    class EventEntry {
        private final Function callback;
        private final Object context;
        private final Object ctx;

        EventEntry(Function callback, Object context, Object ctx) {
            this.callback = callback;
            this.context = context;
            this.ctx = ctx;
        }
    }

    /**
     * if (!eventsApi(this, 'on', name, [callback, context]) || !callback) return this;
     this._events || (this._events = {});
     var events = this._events[name] || (this._events[name] = []);
     events.push({ callback: callback, context: context, ctx: context || this });
     return this;
     */
    public Events on(String name, Function callback) {
        return on(name, callback, null);
    }
    public Events on(String name, Function callback, Object context) {

        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                on(splittedName, callback, context);
            }
        } else {
            List<EventEntry> eventEntries = events.get(name);
            if(eventEntries == null) {
                eventEntries = new ArrayList<EventEntry>();
                events.put(name, eventEntries);
            }

            EventEntry eventEntry = new EventEntry(callback, context, context != null ? context : this);
            eventEntries.add(eventEntry);
        }
        return this;
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
    public Events once(final String name, final Function callback, Object context) {
        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                once(splittedName, callback, context);
            }
        } else {
            final Function onceCallback = new Function() {
                @Override
                public void f() {
                    off(name, this);
                    callback.f(getArguments());
                }
            };
            on(name, onceCallback, context);
        }
        return this;
    }


    /**
     * off(name?: string, callback?: (...args: any[]) => void , context?: any): any {
         var retain, ev, events, names, i, l, j, k;
         if (!this._events || !eventsApi(this, 'off', name, [callback, context])) return this;
         if (!name && !callback && !context) {
             this._events = {};
             return this;
         }

         names = name ? [name] : _.keys(this._events);

         for (i = 0, l = names.length; i < l; i++) {
             name = names[i];
             if (events = this._events[name]) {
                 this._events[name] = retain = [];
                 if (callback || context) {
                     for (j = 0, k = events.length; j < k; j++) {
                        ev = events[j];
                        if ((callback && callback !== ev.callback && callback !== ev.callback._callback) ||
                                (context && context !== ev.context)) {
                            retain.push(ev);
                        }
                     }
                 }
                 if (!retain.length) delete this._events[name];
             }
         }

         return this;
     }
     */
    public Events off() {
        events = new HashMap();
        return this;
    }

    public Events off(Function callback, Object context) {
        String[] names = (String[]) events.keySet().toArray();
        for (String name : names) {
            off(name, callback, context);
        }
        return this;
    }

    public Events off(String name, Function callback) {
        return off(name, callback, null);
    }

    public Events off(String name, Function callback, Object context) {
        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                off(splittedName, callback, context);
            }
        } else {
            List<EventEntry> eventEntries = events.get(name);
            if(eventEntries == null)
                eventEntries = new ArrayList<EventEntry>();

            int retainIndex = 0;
            EventEntry[] retainKey = new EventEntry[eventEntries.size()];

            if(eventEntries.size() > 0 && (callback != null || context != null)) {

                for (int i = 0; i < eventEntries.size(); i++) {
                    EventEntry eventEntry = eventEntries.get(i);
                    if((callback != null && !callback.equals(eventEntry.callback))
                            || (context != null && !context.equals(eventEntry.context)))
                        retainKey[retainIndex] = eventEntry;
                }
                eventEntries.clear();
            }

            if(retainKey.length == 0) events.remove(name);
        }
        return this;
    }


    /**
     trigger(name: string, ...args: any[]): any {
         if (!this._events) return this;
         if (!eventsApi(this, 'trigger', name, args)) return this;

         var events = this._events[name];
         var allEvents = this._events.all;

         if (events) triggerEvents(events, args);
         if (allEvents) triggerEvents(allEvents, arguments);

         return this;
     }
     */
    public Events trigger(String name, Object ...args) {
        if(events == null || events.isEmpty())
            return this;

        // Handle a situation where name is separated, can be multiple events
        if(eventsSplitter.test(name)) {
            String[] names = name.split(eventsSplitter.getSource());
            for (String splittedName : names) {
                trigger(splittedName, args);
            }
        } else {
            List<EventEntry> eventEntries = events.get(name);
            if(eventEntries != null && eventEntries.size() > 0) {
                triggerEvents(eventEntries, args);
            }

            // support for all events listeners
            List<EventEntry> allEventEntries = events.get("all");
            if(allEventEntries != null && allEventEntries.size() > 0) {
                triggerEvents(allEventEntries, ArrayUtils.joinArrays(new Object[] {name}, args));
            }
        }
        return this;
    }

    /**
     *
     stopListening(obj?: any, name?: string, callback?: (...args: any[]) => void ): any {
         var listeners = this._listeners;
         if (!listeners) return this;
         var deleteListener = !name && !callback;

         if (typeof name === 'object') callback = <any> this;
         if (obj) (listeners = {})[obj._listenerId] = obj;

         for (var id in listeners) {
             listeners[id].off(name, callback, this);
             if (deleteListener) delete this._listeners[id];
         }
         return this;
     }
     */
    public Events stopListening() {
        if(listeners == null || listeners.isEmpty())
            return this;

        for (String listenerId : listeners.keySet()) {
            Events listener = listeners.get(listenerId);
            listener.off(null, this);
            listeners.remove(listenerId);
        }

        return this;
    }
    public Events stopListening(Events obj) {
        if(listeners == null || listeners.isEmpty())
            return this;

        obj.off(null, this);
        listeners.remove(obj.listenerId);

        return this;
    }
    public Events stopListening(Events obj, String name) {
        if(listeners == null || listeners.isEmpty())
            return this;

        obj.off(name, null, this);
        return this;
    }

    public Events stopListening(Events obj, String name, Function callback) {
        if(listeners == null || listeners.isEmpty())
            return this;

        obj.off(name, callback, this);
        return this;
    }

    /**
     * listenTo(name: string, callback: (...args: any[]) => void , context?: any): any { }
     * listenToOnce(name: string, callback: (...args: any[]) => void , context?: any): any { }
     *
     * var listenMethods = { listenTo: 'on', listenToOnce: 'once' };

     _.each(listenMethods, function (implementation, method) {
         EventBase.prototype[method] = function (obj, name, callback) {
             var listeners = this._listeners || (this._listeners = {});
             var id = obj._listenerId || (obj._listenerId = _.uniqueId('l'));

             listeners[id] = obj;
             if (typeof name === 'object') callback = this;

             obj[implementation](name, callback, this);
             return this;
         };
     });
     */

    public Events listenTo(Events obj, String name, Function callback) {
        listeners.put(obj.listenerId, obj);
        obj.on(name, callback, this);

        return this;
    }
    public Events listenToOnce(Events obj, String name, Function callback) {
        listeners.put(obj.listenerId, obj);
        obj.once(name, callback, this);

        return this;
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


}
