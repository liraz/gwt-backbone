package org.lirazs.gbackbone.reflection.client;

/**
 * 
 * @author James Luo
 * 
 */
public interface AnnotationType<T> extends ClassType<T> {
	
	/**
	 * Create a new Annotation.
	 * 
	 * @param params the params must be exactly same order of the methods
	 * @return
	 */
	public T createAnnotation(Object[] params);
}
