package org.lirazs.gbackbone.reflection.client;


public interface EnumType<T> extends ClassType<T> {
	public EnumConstant[] getEnumConstants();
}
