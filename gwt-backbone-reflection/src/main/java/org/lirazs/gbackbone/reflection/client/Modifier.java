package org.lirazs.gbackbone.reflection.client;


import org.lirazs.gbackbone.reflection.client.impl.TypeOracleImpl;


/**
 * The Modifier class provides <code>static</code> methods and
 * constants to decode class and member access modifiers.  The sets of
 * modifiers are represented as integers with distinct bit positions
 * representing different modifiers.  The values for the constants
 * representing the modifiers are taken from <a
 * href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/VMSpecTOC.doc.html"><i>The
 * Java</i><sup><small>TM</small></sup> <i>Virtual Machine Specification, Second
 * edition</i></a> tables 
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#75734">4.1</a>,
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#88358">4.4</a>,
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#75568">4.5</a>, and 
 * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#88478">4.7</a>.
 *
 * @see Class#getModifiers()
 * @see Member#getModifiers()
 *
 * @author Nakul Saraiya
 * @author Kenneth Russell
 */
public class Modifier {
	
  public static boolean isPrivate(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_PRIVATE);
  }

  public static boolean isProtected(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_PROTECTED);
  }

  public static boolean isPublic(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_PUBLIC);
  }

  public static boolean isStatic(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_STATIC);
  }
  
  public static boolean isAbstract(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_ABSTRACT);
  }
  
  public static boolean isFinal(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_FINAL);
  }
  

  public static boolean isNative(int modifierBits) {
    return 0 != (modifierBits & TypeOracleImpl.MOD_NATIVE);
  }

}
