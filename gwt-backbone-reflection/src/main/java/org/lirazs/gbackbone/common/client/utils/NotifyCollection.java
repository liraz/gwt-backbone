package org.lirazs.gbackbone.common.client.utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class NotifyCollection<T extends Object> extends ArrayList<NotifyListener<T>> {
	 public void fireNotifyEvent(T sender){
		 for (NotifyListener<T> l : this){
			 l.onNotify(sender);
		 }
	 }
}
