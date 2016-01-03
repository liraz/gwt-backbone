package org.lirazs.gbackbone.client.core.test.router;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.navigation.Router;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created on 18/12/2015.
 */
public class TestRouter extends Router {
    private int loadUrlCount;

    private int count;
    private int testing;

    private String query;
    private String page;
    private String charType;
    private String contact;
    private String arg;
    private String args;
    private String z;

    private String repo;
    private String from;
    private String to;

    private String named;
    private String path;

    private String first;
    private String part;
    private String rest;

    private String entity;
    private String queryArgs;

    private String anything;
    private String value = "unset";

    public TestRouter(Options options) {
        super(options);
    }

    @Override
    protected Map<String, ?> routes() {
        Map<String, Object> routes = new LinkedHashMap<String, Object>();

        routes.put("noCallback", "noCallback");
        routes.put("counter", "counter");
        routes.put("search/:query", "search");
        routes.put("search/:query/p:page", "search");
        routes.put("charñ", "charUTF");
        routes.put("char%C3%B1", "charEscaped");
        routes.put("contacts", "contacts");
        routes.put("contacts/new", "newContact");
        routes.put("contacts/:id", "loadContact");
        routes.put("route-event/:arg", "routeEvent");
        routes.put("optional(/:item)", "optionalItem");
        routes.put("named/optional/(y:z)", "namedOptional");
        routes.put("splat/*args/end", "splat");
        routes.put(":repo/compare/*from...*to", "github");
        routes.put("decode/:named/*splat", "decode");
        routes.put("*first/complex-*part/*rest", "complex");
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
        testing = options.getInt("testing");
        route("implicit", "implicit");
    }



    public int getTesting() {
        return testing;
    }

    public int getCount() {
        return count;
    }

    public String getQuery() {
        return query;
    }

    public String getPage() {
        return page;
    }

    public String getCharType() {
        return charType;
    }

    public String getContact() {
        return contact;
    }

    public String getArg() {
        return arg;
    }

    public String getArgs() {
        return args;
    }

    public String getZ() {
        return z;
    }

    public String getRepo() {
        return repo;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getNamed() {
        return named;
    }

    public String getPath() {
        return path;
    }

    public String getFirst() {
        return first;
    }

    public String getPart() {
        return part;
    }

    public String getRest() {
        return rest;
    }

    public String getEntity() {
        return entity;
    }

    public String getQueryArgs() {
        return queryArgs;
    }

    public String getAnything() {
        return anything;
    }

    public String getValue() {
        return value;
    }

    public void counter() {
        count++;
    }

    public void implicit() {
        count++;
    }

    public void search(String query) {
        this.query = query;
    }

    public void search(String query, String page) {
        this.query = query;
        this.page = page;
    }

    public void charUTF() {
        this.charType = "UTF";
    }

    public void charEscaped() {
        this.charType = "escaped";
    }

    public void contacts() {
        this.contact = "index";
    }

    public void newContact() {
        this.contact = "new";
    }

    public void loadContact(String id) {
        this.contact = "load";
    }

    public void optionalItem(String arg) {
        this.arg = arg;
    }

    public void splat(String args) {
        this.args = args;
    }

    public void github(String repo, String from, String to) {
        this.repo = repo;
        this.from = from;
        this.to = to;
    }

    public void complex(String first, String part, String rest) {
        this.first = first;
        this.part = part;
        this.rest = rest;
    }

    public void query(String entity, String args) {
        this.entity = entity;
        this.queryArgs = args;
    }

    public void anything(String whatever) {
        this.anything = whatever;
    }

    public void namedOptional(String z) {
        this.z = z;
    }

    public void decode(String named, String path) {
        this.named = named;
        this.path = path;
    }

    public void routeEvent(String arg) {
    }
}
