package org.lirazs.gbackbone.client.core.navigation;

/**
 * Created on 18/12/2015.
 */
public interface WindowLocation {

    String getHref();

    String getPath();

    void replace(String newURL);

    String getHash();

    void setHash(String hash);

    void assign(String newURL);
}
