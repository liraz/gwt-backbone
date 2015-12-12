package org.lirazs.gbackbone.client.core.test.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import org.lirazs.gbackbone.client.core.annotation.EventHandler;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.view.View;

/**
 * Created on 12/12/2015.
 */
public class AnnotatedView extends View {
    int[] counter;
    public AnnotatedView(Options options, int[] counter) {
        super(options);
        this.counter = counter;
    }

    @EventHandler("click h1")
    public void foo(Event event, Element element) {
        counter[0]++;

        assert event != null;
        assert element != null;
    }

    @EventHandler("click")
    public void bar(Element element, Event event) {
        counter[0]++;

        assert event != null;
        assert element != null;
    }

    @EventHandler("click")
    public void noArgs() {
        counter[0]++;
    }
}
