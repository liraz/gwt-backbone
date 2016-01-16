package org.lirazs.gbackbone.client.core.test.router;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.RegExp;
import org.lirazs.gbackbone.client.core.navigation.WindowLocation;

/**
 * Created on 18/12/2015.
 */
public class WindowLocationEmulation implements WindowLocation {

    private Element parser;

    private String href;
    private String hash;
    private String host;
    private String search;
    private String fragment;
    private String pathname;
    private String protocol;
    private String lastLocationAssign;

    public WindowLocationEmulation(String href) {
        parser = Document.get().createElement("a");
        replace(href);
    }

    public void replace(String href) {
        setAnchorHref(parser, href);

        //this.href = getAnchorHref(parser);
        this.href = href;
        hash = getAnchorHash(parser);
        host = getAnchorHost(parser);
        search = getAnchorSearch(parser);
        fragment = getAnchorFragment(parser);
        pathname = getAnchorPathname(parser);
        protocol = getAnchorProtocol(parser);

        // In IE, anchor.pathname does not contain a leading slash though
        // window.location.pathname does.
        if(!RegExp.compile("^\\/").test(pathname)) {
            pathname = "/" + pathname;
        }
    }

    @Override
    public String toString() {
        return href;
    }

    public String getHref() {
        return href;
    }

    public String getHash() {
        return hash;
    }

    public String getHost() {
        return host;
    }

    public String getSearch() {
        return search;
    }

    public String getFragment() {
        return fragment;
    }

    public String getPath() {
        return pathname;
    }

    public String getProtocol() {
        return protocol;
    }

    @Override
    public void assign(String newURL) {
        lastLocationAssign = newURL;
        replace(newURL);
    }

    private native String getAnchorHref(Element elem) /*-{
        return elem.href;
    }-*/;
    private native void setAnchorHref(Element elem, String href) /*-{
        elem.href = href;
    }-*/;
    private native String getAnchorHash(Element elem) /*-{
        return elem.hash;
    }-*/;
    private native String getAnchorHost(Element elem) /*-{
        return elem.host;
    }-*/;
    private native String getAnchorSearch(Element elem) /*-{
        return elem.search;
    }-*/;
    private native String getAnchorFragment(Element elem) /*-{
        return elem.fragment;
    }-*/;
    private native String getAnchorPathname(Element elem) /*-{
        return elem.pathname;
    }-*/;
    private native String getAnchorProtocol(Element elem) /*-{
        return elem.protocol;
    }-*/;

    public String getLastLocationAssign() {
        return lastLocationAssign;
    }

    public void setPath(String path) {
        this.pathname = path;
    }

    @Override
    public void setHash(String hash) {
        setAnchorHash(parser, hash);
    }

    private native void setAnchorHash(Element elem, String hash) /*-{
        elem.hash = hash;
    }-*/;
}
