package org.lirazs.gbackbone.client.core.test.router;

import org.lirazs.gbackbone.client.core.annotation.Route;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.navigation.Router;

/**
 * Created on 18/12/2015.
 */
public class AnnotatedTestRouter extends Router {
    private int count;

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

    public AnnotatedTestRouter() {
        super();
    }

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

    @Route("charñ")
    public void charUTF() {
        this.charType = "UTF";
    }

    @Route("char%C3%B1")
    public void charEscaped() {
        this.charType = "escaped";
    }

    @Route("contacts")
    public void contacts() {
        this.contact = "index";
    }

    @Route("contacts/new")
    public void newContact() {
        this.contact = "new";
    }

    @Route("contacts/:id")
    public void loadContact(String id) {
        this.contact = "load";
    }

    @Route("route-event/:arg")
    public void routeEvent(String arg) {
    }

    @Route("optional(/:item)")
    public void optionalItem(String arg) {
        this.arg = arg;
    }

    @Route("named/optional/(y:z)")
    public void namedOptional(String z) {
        this.z = z;
    }

    @Route("splat/*args/end")
    public void splat(String args) {
        this.args = args;
    }

    @Route(":repo/compare/*from...*to")
    public void github(String repo, String from, String to) {
        this.repo = repo;
        this.from = from;
        this.to = to;
    }

    @Route("decode/:named/*splat")
    public void decode(String named, String path) {
        this.named = named;
        this.path = path;
    }

    @Route("*first/complex-*part/*rest")
    public void complex(String first, String part, String rest) {
        this.first = first;
        this.part = part;
        this.rest = rest;
    }

    @Route("query/:entity")
    public void query(String entity, String args) {
        this.entity = entity;
        this.queryArgs = args;
    }

    @Route("*anything")
    public void anything(String whatever) {
        this.anything = whatever;
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

}
