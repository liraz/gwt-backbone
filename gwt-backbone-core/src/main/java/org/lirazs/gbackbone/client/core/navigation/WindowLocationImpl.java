package org.lirazs.gbackbone.client.core.navigation;

import com.google.gwt.user.client.Window;

/**
 * Created on 18/12/2015.
 */
public class WindowLocationImpl implements WindowLocation {
    @Override
    public String getHref() {
        return Window.Location.getHref();
    }

    @Override
    public String getPath() {
        return Window.Location.getPath();
    }

    @Override
    public void replace(String newURL) {
        Window.Location.replace(newURL);
    }

    @Override
    public String getHash() {
        return Window.Location.getHash();
    }

    @Override
    public void assign(String newURL) {
        Window.Location.assign(newURL);
    }
}
