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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import org.lirazs.gbackbone.client.core.data.Options;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 05/01/2016.
 */
public class TemplateFactory {
    private static final Map<String, Template> CACHED_TEMPLATES = new HashMap<String, Template>();

    private static final Map<String, String> TEMPLATE_SETTINGS = new HashMap<String, String>() { {
        put("evaluate", "<%([\\s\\S]+?)%>");
        put("interpolate", "<%=([\\s\\S]+?)%>");
        put("escape", "<%-([\\s\\S]+?)%>");
        put("variable", null); // by default put all values in local scope
        put("urlRoot", ""); // by default no root for urls, urls are absolute/relative with no root
    } };

    private static void setTemplateKey(final Map<String, String> templateSettings, final String key) {
        if (templateSettings.containsKey(key)) {
            TEMPLATE_SETTINGS.put(key, templateSettings.get(key));
        }
    }

    public static void clearTemplateCache() {
        CACHED_TEMPLATES.clear();
    }

    public static void templateSettings(final Map<String, String> templateSettings) {
        setTemplateKey(templateSettings, "evaluate");
        setTemplateKey(templateSettings, "interpolate");
        setTemplateKey(templateSettings, "escape");
        setTemplateKey(templateSettings, "variable");
        setTemplateKey(templateSettings, "urlRoot");
    }

    public static Promise loadTemplate(final String filePath) {
        return loadTemplate(filePath, null);
    }
    public static Promise loadTemplate(final String filePath, final Options templateSettings) {
        Promise promise;
        String urlRoot = TEMPLATE_SETTINGS.get("urlRoot");

        if(templateSettings != null) {
            if (templateSettings.containsKey("urlRoot"))
                urlRoot = templateSettings.get("urlRoot", String.class);
        }
        final String finalFilePath = urlRoot + filePath;

        if(CACHED_TEMPLATES.containsKey(finalFilePath)) {
            promise = new PromiseFunction() {
                @Override
                public void f(Deferred dfd) {
                    dfd.notify(1);
                    dfd.resolve(CACHED_TEMPLATES.get(finalFilePath));
                }
            };
        } else {
            promise = new PromiseFunction() {
                @Override
                public void f(final Deferred dfd) {

                    final Ajax.Settings settings = Ajax.createSettings();
                    settings.setUrl(finalFilePath);
                    settings.setType("get");
                    settings.setDataType("text");
                    settings.setSuccess(new Function() {
                        @Override
                        public void f() {
                            String templateString = getArgument(0);
                            Template template = template(templateString, templateSettings);

                            CACHED_TEMPLATES.put(finalFilePath, template);

                            dfd.notify(1);
                            dfd.resolve(template);
                        }
                    });
                    GQuery.ajax(settings);
                }
            };
        }

        return promise;
    }

    public static Template template(final GQuery selector) {
        return new TemplateImpl(selector.html());
    }
    public static Template template(final GQuery selector, Map<String, String> settings) {
        return new TemplateImpl(selector.html(), new Options(settings));
    }
    public static Template template(final GQuery selector, Options settings) {
        return new TemplateImpl(selector.html(), settings);
    }

    public static Template template(final String template) {
        return new TemplateImpl(template);
    }
    public static Template template(final String template, Map<String, String> settings) {
        return new TemplateImpl(template, new Options(settings));
    }
    public static Template template(final String template, Options settings) {
        return new TemplateImpl(template, settings);
    }

    private static final class TemplateImpl implements Template {
        private final String template;
        private final Options settings;

        private TemplateImpl(String template) {
            this.template = template;
            this.settings = null;
        }
        private TemplateImpl(String template, Options settings) {
            this.template = template;
            this.settings = settings;
        }

        @Override
        public String apply() {
            return apply(new Options());
        }

        @SuppressWarnings("unchecked")
        public String apply(Options attributes) {
            String evaluate = TEMPLATE_SETTINGS.get("evaluate");
            String interpolate = TEMPLATE_SETTINGS.get("interpolate");
            String escape = TEMPLATE_SETTINGS.get("escape");
            String variable = TEMPLATE_SETTINGS.get("variable");

            if(settings != null) {
                if(settings.containsKey("evaluate"))
                    evaluate = settings.get("evaluate", String.class);
                if(settings.containsKey("interpolate"))
                    interpolate = settings.get("interpolate", String.class);
                if(settings.containsKey("escape"))
                    escape = settings.get("escape", String.class);
                if(settings.containsKey("variable"))
                    variable = settings.get("variable", String.class);
            }

            return render(template, escape, interpolate,
                    evaluate, variable, attributes.toProperties());
        }
    }

    public static native String render(String text, String escape, String interpolate, String evaluate, String variable, JavaScriptObject attributes) /*-{
        // Certain characters need to be escaped so that they can be put into a
        // string literal.
        var escapes = {
            "'": "'",
            '\\': '\\',
            '\r': 'r',
            '\n': 'n',
            '\u2028': 'u2028',
            '\u2029': 'u2029'
        };

        var escapeRegExp = /\\|'|\r|\n|\u2028|\u2029/g;

        var escapeChar = function(match) {
            return '\\' + escapes[match];
        };

        $wnd._org_lirazs_gbackbone_client_core_view_TemplateFactory_escape = function (str) {
            return @org.lirazs.gbackbone.client.core.view.TemplateFactory::escape(Ljava/lang/String;)(str);
        };

        // When customizing `templateSettings`, if you don't want to define an
        // interpolation, evaluation or escaping regex, we need one that is
        // guaranteed not to match.
        var noMatch = "(.)^";

        // Combine delimiters into one regular expression via alternation.
        var matcher = RegExp([
                (escape || noMatch),
                (interpolate || noMatch),
                (evaluate || noMatch)
            ].join('|') + '|$', 'g');

        // Compile the template source, escaping string literals appropriately.
        var index = 0;
        var source = "__p+='";
        text.replace(matcher, function(match, escape, interpolate, evaluate, offset) {
            source += text.slice(index, offset).replace(escapeRegExp, escapeChar);
            index = offset + match.length;

            if (escape) {
                source += "'+\n((__t=(" + escape + "))==null?'':$wnd._org_lirazs_gbackbone_client_core_view_TemplateFactory_escape(__t))+\n'";
            } else if (interpolate) {
                source += "'+\n((__t=(" + interpolate + "))==null?'':__t)+\n'";
            } else if (evaluate) {
                source += "';\n" + evaluate + "\n__p+='";
            }

            // Adobe VMs need the match returned to produce the correct offset.
            return match;
        });
        source += "';\n";

        // If a variable is not specified, place data values in local scope.
        if (!variable) source = 'with(obj||{}){\n' + source + '}\n';

        source = "var __t,__p='',__j=Array.prototype.join," +
            "print=function(){__p+=__j.call(arguments,'');};\n" +
            source + 'return __p;\n';

        var render;
        try {
            render = new Function(variable || 'obj', '_', source);
        } catch (e) {
            e.source = source;
            throw e;
        }

        var template = function(data) {
            //return render.call(this, data, _);
            return render.call(this, data, {});
        };

        // Provide the compiled source as a convenience for precompilation.
        var argument = variable || 'obj';
        template.source = 'function(' + argument + '){\n' + source + '}';

        return template(attributes);
    }-*/;

    public static native String escape(String text) /*-{
        // List of HTML entities for escaping.
        var escapeMap = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#x27;',
            '`': '&#x60;'
        };

        // Functions for escaping and unescaping strings to/from HTML interpolation.
        var createEscaper = function(map) {
            var escaper = function(match) {
                return map[match];
            };
            // Regexes for identifying a key that needs to be escaped
            var keys = [];
            for(var key in map) {
                keys.push(key);
            }
            var source = '(?:' + keys.join('|') + ')';

            var testRegexp = RegExp(source);
            var replaceRegexp = RegExp(source, 'g');
            return function(string) {
                string = string == null ? '' : '' + string;
                return testRegexp.test(string) ? string.replace(replaceRegexp, escaper) : string;
            };
        };

        return createEscaper(escapeMap)(text);
    }-*/;
}
