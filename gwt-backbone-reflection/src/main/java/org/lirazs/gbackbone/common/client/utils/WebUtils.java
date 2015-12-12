package org.lirazs.gbackbone.common.client.utils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Random;

public class WebUtils {
	
	public static Element createElement(String html) {
    Element div = DOM.createDiv();
    DOM.setInnerHTML(div, html);
    Element firstChild = DOM.getFirstChild(div);
    return (firstChild != null) ? firstChild : div;
  }
	
	
	public static String getRandomElementID(){
		return "GWTElement" + Random.nextInt();
	}
}
