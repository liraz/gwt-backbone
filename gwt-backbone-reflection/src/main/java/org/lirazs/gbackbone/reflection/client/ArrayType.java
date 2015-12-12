package org.lirazs.gbackbone.reflection.client;


/**
 * An array type
 * 
 * @author James Luo
 *
 */
public interface ArrayType extends ClassType{
	
	public Type getComponentType();
	
	public int getRank();
}
