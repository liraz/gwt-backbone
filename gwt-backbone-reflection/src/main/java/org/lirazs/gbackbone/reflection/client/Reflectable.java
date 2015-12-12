package org.lirazs.gbackbone.reflection.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for Class.
 * 
 *  <li>To pointer out what types relation to this class need reflection informations
 *  <li>To pointer out if need reflection informations of annotations, fields, methods and constructors 
 *  
 *  @see HasReflect
 * 
 * Size Control
 * <li>if you put this annotation to your class, no matter if you use this class in your application, gwtent will put it into reflection system, and GWT compiler have to create the javascript for this class.
 * 
 * @author JamesLuo.au@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Reflectable {
	
  /**
	 * True if you want generate class annotations information
	 */
	public boolean classAnnotations() default true;
	
  
  /**
   * True if you want create info of Fields
   */
  public boolean fields() default true;
  
  /**
   * True if you want create Reflection info of Methods
   */
  public boolean methods() default true;
  
  /**
   * True if you want create Reflection info of Constructors
   */
  public boolean constructors() default true;
  
  
  /**
   * True if you want generate Field and Method annotation information
   */
  public boolean fieldAnnotations() default true;
  
  

  /**
   * If relationTypes is true, 
   * All the types of field, return types of Methods, parameter types
   * of Methods will generate reflection information
   * 
   * GWTENT will create full reflection information of that type, 
   * <p>For example, you have a function like this and relationTypes set to true
   * <p>public String dateToString(Date date);
   * <br>
   * this equal:
   * <br>@Reflect_Full
   * <br>public class String
   * <br>@Reflect_Full
   * <br>public class Date
   * 
   * <br> class "String" and "Date" will created reflection will "full" settings
   * 
   * <br> For Library developers, if you don't want your class create "full" reflection(For reduce javascript size)
   * please using @ReflectCandidate
   * 
   * @see ReflectCandidate
   */
  public boolean relationTypes() default false;
  
  
  /**
   * If this is true, All super classes to TObject will generate reflection information
   * default false
   */
  public boolean superClasses() default false;
  
  
  /**
   * if this is true, for different targets this annotation apply to:
   * 
   * <li>if annotate to a class, this is mean All subClasses will generate reflection information
   * <li>if annotate to a interface, then all implement classes will generate reflection information,
   * <li>if annotate to an annotation, then all types annotated by this annotation will generate reflection information
   * 
   * <p>default false
   */
  public boolean assignableClasses() default false;

}
