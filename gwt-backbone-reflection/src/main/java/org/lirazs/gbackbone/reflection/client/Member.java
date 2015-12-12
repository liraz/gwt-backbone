package org.lirazs.gbackbone.reflection.client;

/**
 * Member is an interface that reflects identifying information about
 * a single member (a field or a method) or a constructor.
 * 
 * @author James Luo
 *
 * 11/08/2010 2:28:04 PM
 */
public interface Member {
	/**
   * Returns the Class object representing the class or interface
   * that declares the member or constructor represented by this Member.
   *
   * @return an object representing the declaring class of the
   * underlying member
   */
  public Class<?> getDeclaringClass();

  /**
   * Returns the simple name of the underlying member or constructor
   * represented by this Member.
   * 
   * @return the simple name of the underlying member
   */
  public String getName();

  /**
   * Returns the Java language modifiers for the member or
   * constructor represented by this Member, as an integer.  The
   * Modifier class should be used to decode the modifiers in
   * the integer.
   * 
   * @return the Java language modifiers for the underlying member
   * @see Modifier
   */
  public int getModifiers();
}
