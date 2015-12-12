package org.lirazs.gbackbone.reflection.client;

/**
 * 
 * @author James Luo
 *
 */
public class ReflectionRequiredException extends RuntimeException{
	/**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public ReflectionRequiredException(){
    super();
  }
  
  public ReflectionRequiredException(String message){
    super(message);
  }
}
