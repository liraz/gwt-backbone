package org.lirazs.gbackbone.common.client.utils;

/**
 * Utils class for get version of current browser
 * @author JamesLuo.au@gmail.com
 *
 */
public class UserAgent {
	
  public native static String getUserAgent() /*-{
  	return $wnd.navigator.userAgent.toLowerCase();
	}-*/;
  
  
  public static boolean isOpera(){
  	doInit();
  	return fisOpera;
  }
  
  public static boolean isIE(){
  	doInit();
		return fisIE;
  }
  
  public static boolean isIE6(){
  	doInit();
		return fisIE6;
  }
  
  
  public static boolean isIE7(){
  	doInit();
		return fisIE7;
  }
  
  
  public static boolean isIE8(){
  	doInit();
		return fisIE8;
  }
  
  public static boolean isChrome(){
  	doInit();
		return fisChrome;
  }
  
  public static boolean isSafari(){
  	doInit();
		return fisSafari;
  }
  
  public static boolean isIPhone(){
  	doInit();
		return fisIPhone;
  }
  
  public static boolean isAndroid(){
  	doInit();
		return fisAndroid;
  }
  
  public static boolean isMobile(){
  	doInit();
		return fisMobile;
  }
 
  
  
  
  private static boolean inited = false;
  private static void doInit(){
  	if (! inited){
  		inited = true;
  		String userAgent = getUserAgent();   
      
      fisOpera = userAgent.indexOf("opera") > -1;
      fisChrome = userAgent.indexOf("chrome") > -1;
      fisSafari = !fisChrome && (userAgent.indexOf("webkit") > -1 || userAgent.indexOf("khtml") > -1);
      
      fisIE = !fisOpera && userAgent.indexOf("msie") > -1;
      fisIE7 = !fisOpera && userAgent.indexOf("msie 7") > -1;
      fisIE8 = !fisOpera && userAgent.indexOf("msie 8") > -1;
      fisIE6 = fisIE && !fisIE7 && !fisIE8;
  	}
  }
  
  private static boolean fisOpera;
  private static boolean fisIE;
  private static boolean fisIE6;
  private static boolean fisIE7;
  private static boolean fisIE8;
  private static boolean fisIPhone;
  private static boolean fisAndroid;
  private static boolean fisMobile;
  private static boolean fisChrome;
  private static boolean fisSafari;
}
