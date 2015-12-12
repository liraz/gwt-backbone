package org.lirazs.gbackbone.reflection.client.impl;

import org.lirazs.gbackbone.reflection.client.EnumConstant;

public class EnumConstantImpl extends FieldImpl implements EnumConstant {
	public EnumConstantImpl(ClassTypeImpl enclosingType, String name, int ordinal) {
		super(enclosingType, name);
		
		this.ordinal = ordinal;
	}

	private final int ordinal;
	
	public EnumConstant isEnumConstant() {
		return this;
	}

	public int getOrdinal() {
		return ordinal;
	}

}
