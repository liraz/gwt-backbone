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
package org.lirazs.gbackbone.navigation.client;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.navigation.Router;
import org.lirazs.gbackbone.client.core.navigation.function.OnRouteFunction;
import org.lirazs.gbackbone.client.core.util.RouterUtils;
import org.lirazs.gbackbone.client.core.util.StringUtils;
import org.lirazs.gbackbone.client.core.util.UUID;
import org.lirazs.gbackbone.client.core.view.TemplateFactory;
import org.lirazs.gbackbone.navigation.client.annotation.Navigate;
import org.lirazs.gbackbone.navigation.client.storage.BrowserStorage;
import org.lirazs.gbackbone.navigation.client.storage.CookieBrowserStorage;
import org.lirazs.gbackbone.navigation.client.storage.LocalBrowserStorage;
import org.lirazs.gbackbone.reflection.client.*;

import java.util.*;

import static org.lirazs.gbackbone.client.core.data.Options.O;


/**
 * Created on 11/01/2016.
 */
public class Navigation {
    private static final String DEFAULT_HASH_PREFIX = "#!";

    private static final String DEFAULT_PAGE_CONTEXT = "/";
    private static final String DEFAULT_STORAGE_PREFIX_FORMAT = "BN_%s";

    //TODO: Usage example 1
    /*this.navigation = new Backbone.Navigation({
        storagePrefixFormat : "BNC_OP_{0}"
    });*/

    //TODO: Usage example 2
    /*var navigation = App.getNavigation();
    navigation.appendRouter(this.router);
    navigation.mapRouters();*/

    private String storagePrefixFormat = DEFAULT_STORAGE_PREFIX_FORMAT;
    private String hashPrefix = DEFAULT_HASH_PREFIX;

    private int maxPageEntries = 30; // the maximum page entries that are allowed - will cut the tail once it gets larger than maximum

    private final List<Router> routers;
    private final Map<String, Navigate> tree; // key - the route, value -the annotation associated to the route

    private final BrowserStorage browserStorage;
    private Collection<Model> breadcrumbs;

    // the uuid the server provided for the client page rendered in the browser.
    // this is required for mechanism to determine browser back operations
    private final String serverPageUuid;

    // a root context of the page in the browser
    private final String pageContext;

    /**
     *
     */
    public Navigation() {
        // by default we store the all navigation data in local storage of browser
        this(UUID.uuid(), NavigationStorageMode.LOCAL_STORAGE);
    }

    /**
     *
     * @param serverPageUuid
     */
    public Navigation(String serverPageUuid) {
        // by default we store the all navigation data in local storage of browser
        this(serverPageUuid, NavigationStorageMode.LOCAL_STORAGE);
    }

    /**
     *
     * @param serverPageUuid
     * @param storageMode
     */
    public Navigation(String serverPageUuid, NavigationStorageMode storageMode) {
        this(serverPageUuid, storageMode, DEFAULT_PAGE_CONTEXT);
    }

    /**
     *
     * @param serverPageUuid
     * @param pageContext
     */
    public Navigation(String serverPageUuid, String pageContext) {
        this(serverPageUuid, NavigationStorageMode.LOCAL_STORAGE, pageContext);
    }

    /**
     *
     * @param serverPageUuid
     * @param storageMode
     * @param pageContext
     */
    public Navigation(String serverPageUuid, NavigationStorageMode storageMode, String pageContext) {
        this.serverPageUuid = serverPageUuid;
        this.pageContext = pageContext;

        browserStorage = storageMode == NavigationStorageMode.LOCAL_STORAGE ? new LocalBrowserStorage() :
                new CookieBrowserStorage(pageContext);

        routers = new ArrayList<>();
        tree = new HashMap<>();
        breadcrumbs = new Collection<>();

        // build the breadcrumbs from storage on initialize
        buildBreadcrumbsFromStorage();

        // check if user tried to do a back with browser
        checkBackNavigationFromBrowser();
    }

    /**
     *
     * @param storagePrefixFormat
     */
    public void setStoragePrefixFormat(String storagePrefixFormat) {
        this.storagePrefixFormat = storagePrefixFormat;
    }

    /**
     *
     * @param hashPrefix
     */
    public void setHashPrefix(String hashPrefix) {
        this.hashPrefix = hashPrefix;
    }

    /**
     *
     * @return
     */
    public List<Router> getAllRouters() {
        return routers;
    }

    /**
     *
     * @param router
     */
    public void appendRouter(Router router) {

        try {
            ClassType classType = TypeOracle.Instance.getClassType(router.getClass());
            Method[] methods = classType.getMethods();

            for (final Method method : methods) {
                Navigate annotation = method.getAnnotation(Navigate.class);

                if(annotation != null) {
                    String methodName = method.getName();
                    String route = router.getRouteByMethodName(methodName);

                    if (route != null) {
                        tree.put(route, annotation);
                    }
                }
            }
            routers.add(router);

        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
    }

    /**
     *
     */
    public void mapRouters() {
        for (Router router : routers) {
            router.onRoute(new OnRouteFunction() {
                @Override
                public void f(Router router, String route, String[] args) {
                    String url = getRouteUrl(router, route);

                    Options mappedArgs = getMappedArgs(args, url);
                    mapNavigation(router, route, mappedArgs);
                }
            });
        }
    }

    /**
     *
     * @return the accumulated breadcrumbs collection
     */
    public Collection<Model> getBreadcrumbs() {
        return breadcrumbs;
    }

    /**
     *
     * @return the last page in the breadcrumbs list
     */
    public Model getCurrentPage() {
        if(breadcrumbs.size() > 0) {
            return breadcrumbs.last();
        }
        return null;
    }

    /**
     * Persist data into page entry custom data map.
     * If not specifying a page, currentPage will be used.
     *
     * @param key
     * @param value
     * @param page
     */
    public void putDataToPage(String key, Options value, Model page) {
        // current page will be used on default
        if(page == null)
            page = getCurrentPage();

        if(page != null) {
            Options pageData = page.get("data");
            pageData.put(key, value);

            // Update the page's storage
            String storageKey = StringUtils.format(storagePrefixFormat, page.get("index"));
            browserStorage.setItem(storageKey, page.toJSON());
        }
    }

    /**
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getCurrentPageData(String key) {
        T value = null;

        Model currentPage = getCurrentPage();
        if(currentPage != null && !key.isEmpty()) {
            Options pageData = currentPage.get("data");
            value = pageData.get(key);
        }
        return value;
    }

    /**
     *
     */
    public void back() {
        int numberOfEntries = breadcrumbs.size();

        if(numberOfEntries > 1) {
            Model oneBeforeLast = breadcrumbs.at(numberOfEntries - 2);
            routeToPageEntry(oneBeforeLast, true);
        }
    }

    private void buildBreadcrumbsFromStorage() {
        int storageIndex = 0;

        String storageKey = StringUtils.format(storagePrefixFormat, storageIndex);
        boolean hasPage = browserStorage.hasItem(storageKey);

        while(hasPage) {
            Options page = browserStorage.getItem(storageKey);
            Model pageModel = new Model(page);

            breadcrumbs.add(pageModel);

            // get the next cookie index
            storageIndex++;
            storageKey = StringUtils.format(storagePrefixFormat, storageIndex);
            hasPage = browserStorage.hasItem(storageKey);
        }
    }

    private void checkBackNavigationFromBrowser() {
        String currentPageUrl = pageContext + Window.Location.getHash();

        // starting from the end - cutting the tail
        for (int i = breadcrumbs.size() - 1; i >= 0; i--) {
            Model pageEntry = breadcrumbs.at(i);
            String pageEntryUrl = pageEntry.getString("url");
            String pageEntryServerUuid = pageEntry.getString("serverPageUuid");

            // searching all page entries, if one of them has the same server uuid
            if(pageEntry.has("url") && currentPageUrl.contains(pageEntryUrl)) {
                boolean pageEntryHasHash = pageEntryUrl.contains(hashPrefix);

                if(pageEntryHasHash || serverPageUuid.equals(pageEntryServerUuid)) {
                    // the user clicked the back button
                    routeToPageEntry(pageEntry, true, true);
                    break; // do not route any page anymore...
                }
            }
        }
    }

    private void syncServerPageUuidWithDuplicatesOfPageEntry(Model pageEntry) {
        String serverPageUuid = pageEntry.getString("serverPageUuid");

        for (int i = 0; i < breadcrumbs.size(); i++) {
            Model breadPageEntry = breadcrumbs.at(i);
            if(Objects.equals(breadPageEntry.getString("url"), pageEntry.getString("url"))) {
                breadPageEntry.set("serverPageUuid", serverPageUuid);

                // Update the page's storage
                String storageKey = StringUtils.format(storagePrefixFormat, breadPageEntry.get("index"));
                browserStorage.setItem(storageKey, breadPageEntry.toJSON());
            }
        }
    }

    private Options getMappedArgs(String[] args, String link) {
        RegExp route = RouterUtils.routeToRegExp(link);
        String[] argNames = RouterUtils.extractParameters(route, link);

        int index = 0;
        Options namedArgs = new Options();

        for (String argName : argNames) {
            namedArgs.put(argName, args[index++]);
        }
        return namedArgs;
    }

    private String getMappedUrl(String url, Options mappedArgs) {
        String result = url;

        for (Map.Entry<String, Object> entry : mappedArgs.entrySet()) {
            String argName = entry.getKey();
            String arg = (String) entry.getValue();

            result = url.replace(argName, arg);
        }
        return result;
    }

    private void mapNavigation(Router router, String route, Options mappedArgs) {

        if(!tree.containsKey(route))
            return;

        Navigate navigate = tree.get(route);

        String template = navigate.template();
        String[] newEntryOnArgsChange = navigate.newEntryOnArgChange();

        Model lastPage = breadcrumbs.last();
        String url = getMappedUrl(getRouteUrl(router, route), mappedArgs);

        Model page = new Model(O(
                "url", url,
                "text", TemplateFactory.template(template).apply(mappedArgs),
                "mappedArgs", mappedArgs,
                "serverPageUuid", serverPageUuid,
                "route", route,
                "index", breadcrumbs.size(),
                "data", O(), // custom page data
                "backNavigation", isBackNavigation()
        ));

        String currentPageServerUuid = page.getString("serverPageUuid");
        String lastPageServerUuid = lastPage != null ? lastPage.getString("serverPageUuid") : null;

        // last page was the same as current page AND current page is not "sub" module AND last page is sub module - doing back..
        if(lastPage != null && !page.getString("url").contains(hashPrefix)
                && lastPage.getString("url").contains(hashPrefix)
                && currentPageServerUuid.equals(lastPageServerUuid)) {

            back();
        } else {
            syncServerPageUuidWithDuplicatesOfPageEntry(page);

            // additional check for entries where a specific attribute causes an entry
            // not to replace it-self, and create a new one (only if changed)
            boolean entryArgValueChanged = false;
            if(lastPage != null && newEntryOnArgsChange.length > 0) {

                for (String newEntryOnArgChange : newEntryOnArgsChange) {
                    String entryArgKeyCheck = ":" + newEntryOnArgChange;

                    Options pageMappedArgs = page.get("mappedArgs");
                    Options lastPageMappedArgs = page.get("mappedArgs");

                    String pageArgValue = pageMappedArgs.get(entryArgKeyCheck);
                    String lastPageArgValue = lastPageMappedArgs.get(entryArgKeyCheck);

                    if(!pageArgValue.equals(lastPageArgValue)) {
                        entryArgValueChanged = true;
                        break;
                    }
                }
            }

            // if page's route is the same as lastPage, replace it
            if(lastPage != null && lastPage.getString("route").equals(page.get("route")) && !entryArgValueChanged) {
                replacePageEntry(lastPage, page, true);
            } else {// add page to storage and breadcrumbs list
                addPageEntry(page);

                // check the tail if it needs to be cut
                checkBreadcrumbsTail();
            }
        }
    }

    private String getRouteUrl(Router router, String route) {
        String[] routes = router.getRoutes();
        for (String url : routes) {
            if(route.equals(url)) {
                return url;
            }
        }
        return null;
    }

    private void addPageEntry(Model page) {
        String storageKey = StringUtils.format(storagePrefixFormat, page.get("index"));
        Options value = page.omit("backNavigation"); // not saving the backNavigation internal flag

        browserStorage.setItem(storageKey, value);
        breadcrumbs.add(page);
    }

    private void removeAll(Options options) {
        List<Model> pagesToRemove = new ArrayList<>();

        for(int index = 0; index < breadcrumbs.size(); index++) {
            Model pageToRemove = breadcrumbs.at(index);
            pagesToRemove.add(pageToRemove);
        }
        removePageEntries(pagesToRemove, options);
    }

    private void removePageEntry(Model page) {
        removePageEntry(page, null);
    }
    private void removePageEntry(Model page, Options options) {
        int index = page.getInt("index");

        String storageKey = StringUtils.format(storagePrefixFormat, index);
        browserStorage.removeItem(storageKey);

        breadcrumbs.remove(page, options);
    }

    private void removePageEntries(List<Model> pagesToRemove) {
        removePageEntries(pagesToRemove, null);
    }
    private void removePageEntries(List<Model> pagesToRemove, Options options) {
        for (Model pageToRemove : pagesToRemove) {
            removePageEntry(pageToRemove, options);
        }
    }

    private void replacePageEntry(Model oldPage, Model newPage, boolean copyPageData) {
        int oldIndex = oldPage.getInt("index");
        Options oldData = oldPage.get("data");

        newPage.set("index", oldIndex);
        if(copyPageData) {
            newPage.set("data", oldData);
        }

        removePageEntry(oldPage);
        addPageEntry(newPage);
    }

    private void routeToPageEntry(final Model page, boolean navigateToPage) {
        routeToPageEntry(page, navigateToPage, false);
    }
    private void routeToPageEntry(final Model page, boolean navigateToPage, boolean deferredNavigation) {
        List<Model> pagesToRemove = new ArrayList<>();
        // Remove all indexes that comes after page
        for (int index = page.getInt("index") + 1; index < breadcrumbs.size(); index++) {
            Model pageToRemove = breadcrumbs.at(index);
            pagesToRemove.add(pageToRemove);
        }

        removePageEntries(pagesToRemove);

        // marking entry as back navigation - since clicking on an entry before, is like going back in navigation a few more steps
        page.set("backNavigation", true);

        if(navigateToPage && routers.size() > 0) {
            final Router router = routers.get(0);

            if(deferredNavigation) {
                Timer t = new Timer() {
                    public void run() {
                        // when doing browser back - the router object is not created
                        // so we need to check here for not null
                        if(router != null) {
                            router.navigate(page.getString("url"), O("trigger", true, "replace", true));
                        }
                    }
                };

                // Schedule the timer to run once in 0 seconds.
                t.schedule(0);
            } else {
                router.navigate(page.getString("url"), O("trigger", true, "replace", true));
            }
        }

        // if it's a different page, requires refresh & browser supports push state
        String pageUrl = page.getString("url");
        if(!pageUrl.contains(pageContext) && !pageContext.contains(pageUrl)
                && isPushStateSupported()) {

            Window.Location.reload();
        }
    }


    private boolean isBackNavigation() {
        boolean backNavigation = false;
        Model currentPage = getCurrentPage();

        if(currentPage != null && currentPage.has("backNavigation")) {
            backNavigation = currentPage.get("backNavigation");
        }
        return backNavigation;
    }


    private void checkBreadcrumbsTail() {
        if(breadcrumbs.size() > maxPageEntries) {
            // Remove all cookies/storage
            for (int i = 0; i < this.breadcrumbs.size(); i++) {
                String storageKeyToRemove = StringUtils.format(storagePrefixFormat, i);
                browserStorage.removeItem(storageKeyToRemove);
            }

            // cut the breadcrumbs tail
            breadcrumbs.shift();

            // update all pages indexes
            for (int j = 0; j < breadcrumbs.size(); j++) {
                Model pageEntry = breadcrumbs.at(j);
                pageEntry.set("index", j);

                String storageKeyToSet = StringUtils.format(storagePrefixFormat, j);
                browserStorage.setItem(storageKeyToSet, pageEntry.toJSON());
            }
        }
    }

    private native boolean isPushStateSupported() /*-{
        return !!($wnd.history && $wnd.history.pushState);
    }-*/;
}
