package org.lirazs.gbackbone.client.core.view;

import com.google.gwt.core.client.JavaScriptObject;
import org.lirazs.gbackbone.client.core.data.Options;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 05/01/2016.
 */
public class TemplateFactory {
    private static final Map<String, String> TEMPLATE_SETTINGS = new HashMap<String, String>() { {
        put("evaluate", "<%([\\s\\S]+?)%>");
        put("interpolate", "<%=([\\s\\S]+?)%>");
        put("escape", "<%-([\\s\\S]+?)%>");
        put("variable", null); // by default put all values in local scope
    } };

    private static void setTemplateKey(final Map<String, String> templateSettings, final String key) {
        if (templateSettings.containsKey(key)) {
            TEMPLATE_SETTINGS.put(key, templateSettings.get(key));
        }
    }

    public static void templateSettings(final Map<String, String> templateSettings) {
        setTemplateKey(templateSettings, "evaluate");
        setTemplateKey(templateSettings, "interpolate");
        setTemplateKey(templateSettings, "escape");
        setTemplateKey(templateSettings, "variable");
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
