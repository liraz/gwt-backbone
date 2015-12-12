package org.lirazs.gbackbone.reflection.client;

import java.lang.annotation.Annotation;

/**
 * Try to simulate the most of functions in java.lang.Class
 * 
 * @author James Luo
 * 
 */
public class ClassHelper<T> implements AnnotatedElement {

	private final ClassType<T> classType;
	private final EnumType enumType;

	private final Type type;
	private final Class<T> clazz;

	private ClassHelper(Type type, Class<T> clazz) {
		this.type = type;
		this.clazz = clazz;

		this.classType = type.isClassOrInterface();
		this.enumType = type.isEnum();
	}

	/**
	 * Supported:
	 * <p>
	 * <ul>
	 * <li>class</li>
	 * <li>interface</li>
	 * <li>enum</li>
	 * <li>array</li>
	 * <li>primitive types</li>
	 * </ul>
	 * </p>
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public static <T> ClassHelper<T> AsClass(Class<T> clazz) {
		ClassType<T> classType = TypeOracle.Instance.getClassType(clazz);

		if (classType == null) {
			ReflectionUtils.checkReflection(clazz);
		}

		return new ClassHelper<T>(classType, clazz);
	}

	public Type getType() {
		return type;
	}

	/**
	 * Returns the <code>Class</code> representing the superclass of the entity
	 * (class, interface, primitive type or void) represented by this
	 * <code>Class</code>. If this <code>Class</code> represents either the
	 * <code>Object</code> class, an interface, a primitive type, or void, then
	 * null is returned. If this object represents an array class then the
	 * <code>Class</code> object representing the <code>Object</code> class is
	 * returned.
	 * 
	 * @return the superclass of the class represented by this object.
	 */
	public Class<? super T> getSuperclass() {
		ClassType result = null;
		if (type instanceof ClassType)
			result = ((ClassType) type).getSuperclass();

		if (result == null)
			return null;
		else
			return (Class<? super T>) result.getDeclaringClass();
	}

	/**
	 * Determines the interfaces implemented by the class or interface represented
	 * by this object.
	 * 
	 * <p>
	 * If this object represents a class, the return value is an array containing
	 * objects representing all interfaces implemented by the class. The order of
	 * the interface objects in the array corresponds to the order of the
	 * interface names in the <code>implements</code> clause of the declaration of
	 * the class represented by this object. For example, given the declaration:
	 * <blockquote>
	 * 
	 * <pre>
	 * class Shimmer implements FloorWax, DessertTopping { ... }
	 * </pre>
	 * 
	 * </blockquote> suppose the value of <code>s</code> is an instance of
	 * <code>Shimmer</code>; the value of the expression: <blockquote>
	 * 
	 * <pre>
	 * s.getClass().getInterfaces()[0]
	 * </pre>
	 * 
	 * </blockquote> is the <code>Class</code> object that represents interface
	 * <code>FloorWax</code>; and the value of: <blockquote>
	 * 
	 * <pre>
	 * s.getClass().getInterfaces()[1]
	 * </pre>
	 * 
	 * </blockquote> is the <code>Class</code> object that represents interface
	 * <code>DessertTopping</code>.
	 * 
	 * <p>
	 * If this object represents an interface, the array contains objects
	 * representing all interfaces extended by the interface. The order of the
	 * interface objects in the array corresponds to the order of the interface
	 * names in the <code>extends</code> clause of the declaration of the
	 * interface represented by this object.
	 * 
	 * <p>
	 * If this object represents a class or interface that implements no
	 * interfaces, the method returns an array of length 0.
	 * 
	 * <p>
	 * If this object represents a primitive type or void, the method returns an
	 * array of length 0.
	 * 
	 * @return an array of interfaces implemented by this class.
	 */
	public Class<?>[] getInterfaces() {
		ClassType<?>[] types = new ClassType<?>[0];
		if (type instanceof ClassType) {
			types = ((ClassType<?>) type).getImplementedInterfaces();
		}

		Class<?>[] result = new Class<?>[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = types[i].getDeclaringClass();
		}
		return result;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (type instanceof HasAnnotations) {
			return ((HasAnnotations) type).getAnnotation(annotationClass);
		}

		return null;
	}

	public Annotation[] getAnnotations() {
		if (type instanceof HasAnnotations) {
			return ((HasAnnotations) type).getAnnotations();
		}

		return new Annotation[0];
	}

	public Annotation[] getDeclaredAnnotations() {
		if (type instanceof HasAnnotations) {
			return ((HasAnnotations) type).getDeclaredAnnotations();
		}

		return new Annotation[0];
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		if (type instanceof HasAnnotations) {
			return ((HasAnnotations) type).isAnnotationPresent(annotationClass);
		}

		return false;
	}

	/**
	 * Determines if the specified <code>Class</code> object represents an
	 * interface type.
	 * 
	 * @return <code>true</code> if this object represents an interface;
	 *         <code>false</code> otherwise.
	 */
	public boolean isInterface() {
		return type.isInterface() != null;
	}

	/**
	 * Determines if the specified <code>Class</code> object represents a
	 * primitive type.
	 * 
	 * <p>
	 * There are nine predefined <code>Class</code> objects to represent the eight
	 * primitive types and void. These are created by the Java Virtual Machine,
	 * and have the same names as the primitive types that they represent, namely
	 * <code>boolean</code>, <code>byte</code>, <code>char</code>,
	 * <code>short</code>, <code>int</code>, <code>long</code>, <code>float</code>
	 * , and <code>double</code>.
	 * 
	 * <p>
	 * These objects may only be accessed via the following public static final
	 * variables, and are the only <code>Class</code> objects for which this
	 * method returns <code>true</code>.
	 * 
	 * @return true if and only if this class represents a primitive type
	 * 
	 * @see Boolean#TYPE
	 * @see Character#TYPE
	 * @see Byte#TYPE
	 * @see Short#TYPE
	 * @see Integer#TYPE
	 * @see Long#TYPE
	 * @see Float#TYPE
	 * @see Double#TYPE
	 * @see Void#TYPE
	 */
	public boolean isPrimitive() {
		return type.isPrimitive() != null;
	}

	/**
	 * Returns the <code>Class</code> object associated with the class or
	 * interface with the given string name. Invoking this method is equivalent
	 * to:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * Class.forName(className, true, currentLoader)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * where <code>currentLoader</code> denotes the defining class loader of the
	 * current class.
	 * 
	 * <p>
	 * For example, the following code fragment returns the runtime
	 * <code>Class</code> descriptor for the class named
	 * <code>java.lang.Thread</code>:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   Class t = Class.forName(&quot;java.lang.Thread&quot;)
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * A call to <tt>forName("X")</tt> causes the class named <tt>X</tt> to be
	 * initialized.
	 * 
	 * @param className
	 *          the fully qualified name of the desired class.
	 * @return the <code>Class</code> object for the class with the specified
	 *         name.
	 * @exception org.lirazs.gbackbone.reflection.client.ReflectionRequiredException
	 *              as the reason of ClassNotFoundException
	 * @exception ClassNotFoundException
	 *              if the class cannot be located
	 */
	public static Class<?> forName(String className)
			throws ClassNotFoundException {
		try {
			ClassType type = TypeOracle.Instance.getClassType(className);
			return type.getDeclaringClass();
		} catch (ReflectionRequiredException e1) {
			throw new ClassNotFoundException("Class not found: " + className + e1.getMessage(), e1);
		}
	}

	/**
	 * Determines if this <code>Class</code> object represents an array class.
	 * 
	 * @return <code>true</code> if this object represents an array class;
	 *         <code>false</code> otherwise.
	 * @since GWTENT RC2
	 */
	public boolean isArray() {
		return clazz.isArray();
	}

	/**
	 * Returns the <code>Class</code> representing the component type of an array.
	 * If this class does not represent an array class this method returns null.
	 * 
	 * @return the <code>Class</code> representing the component type of this
	 *         class if this class is an array
	 * @see java.lang.reflect.Array
	 * @since GWTENT RC2
	 */
	public Class<?> getComponentType() {
		return clazz.getComponentType();
	}

	/**
	 * Returns the <tt>Type</tt> representing the direct superclass of the entity
	 * (class, interface, primitive type or void) represented by this
	 * <tt>Class</tt>.
	 * 
	 * <p>
	 * If the superclass is a parameterized type, the <tt>Type</tt> object
	 * returned must accurately reflect the actual type parameters used in the
	 * source code. The parameterized type representing the superclass is created
	 * if it had not been created before. See the declaration of
	 * {@link java.lang.reflect.ParameterizedType ParameterizedType} for the
	 * semantics of the creation process for parameterized types. If this
	 * <tt>Class</tt> represents either the <tt>Object</tt> class, an interface, a
	 * primitive type, or void, then null is returned. If this object represents
	 * an array class then the <tt>Class</tt> object representing the
	 * <tt>Object</tt> class is returned.
	 * 
	 * @throws GenericSignatureFormatError
	 *           if the generic class signature does not conform to the format
	 *           specified in the Java Virtual Machine Specification, 3rd edition
	 * @throws TypeNotPresentException
	 *           if the generic superclass refers to a non-existent type
	 *           declaration
	 * @throws MalformedParameterizedTypeException
	 *           if the generic superclass refers to a parameterized type that
	 *           cannot be instantiated for any reason
	 * @return the superclass of the class represented by this object
	 * @since 1.5
	 */
	public Type getGenericSuperclass() {
		if (type.isParameterized() != null) {
			// Historical irregularity:
			// Generic signature marks interfaces with superclass = Object
			// but this API returns null for interfaces
			if (isInterface())
				return null;
			
			return type.isParameterized().isClass().getSuperclass();
		} else
			return classType == null ? null : classType.getSuperclass();
	}
	
	/**
   * Returns the <tt>Type</tt>s representing the interfaces 
   * directly implemented by the class or interface represented by
   * this object.
   *
   * <p>If a superinterface is a parameterized type, the
   * <tt>Type</tt> object returned for it must accurately reflect
   * the actual type parameters used in the source code. The
   * parameterized type representing each superinterface is created
   * if it had not been created before. See the declaration of
   * {@link java.lang.reflect.ParameterizedType ParameterizedType}
   * for the semantics of the creation process for parameterized
   * types.
   *
   * <p> If this object represents a class, the return value is an
   * array containing objects representing all interfaces
   * implemented by the class. The order of the interface objects in
   * the array corresponds to the order of the interface names in
   * the <tt>implements</tt> clause of the declaration of the class
   * represented by this object.  In the case of an array class, the
   * interfaces <tt>Cloneable</tt> and <tt>Serializable</tt> are
   * returned in that order.
   *
   * <p>If this object represents an interface, the array contains
   * objects representing all interfaces directly extended by the
   * interface.  The order of the interface objects in the array
   * corresponds to the order of the interface names in the
   * <tt>extends</tt> clause of the declaration of the interface
   * represented by this object.
   *
   * <p>If this object represents a class or interface that
   * implements no interfaces, the method returns an array of length
   * 0.
   *
   * <p>If this object represents a primitive type or void, the
   * method returns an array of length 0.
   * 
   * @return an array of interfaces implemented by this class
   * @since RC2
   */
  public Type[] getGenericInterfaces() {
  	ClassType<?>[] types = new ClassType<?>[0];
		if (type instanceof ClassType) {
			types = ((ClassType<?>) type).getImplementedInterfaces();
		}
		return types;
  }
  
  /**
   * Returns a <code>Method</code> object that reflects the specified public
   * member method of the class or interface represented by this
   * <code>Class</code> object. The <code>name</code> parameter is a
   * <code>String</code> specifying the simple name the desired method. The
   * <code>parameterTypes</code> parameter is an array of <code>Class</code>
   * objects that identify the method's formal parameter types, in declared
   * order. If <code>parameterTypes</code> is <code>null</code>, it is 
   * treated as if it were an empty array.
   *
   * <p> If the <code>name</code> is "&lt;init&gt;"or "&lt;clinit&gt;" a
   * <code>NoSuchMethodException</code> is raised. Otherwise, the method to
   * be reflected is determined by the algorithm that follows.  Let C be the
   * class represented by this object:
   * <OL>
   * <LI> C is searched for any <I>matching methods</I>. If no matching
   * 	    method is found, the algorithm of step 1 is invoked recursively on
   * 	    the superclass of C.</LI>
   * <LI> If no method was found in step 1 above, the superinterfaces of C
   *      are searched for a matching method. If any such method is found, it
   *      is reflected.</LI>
   * </OL>
   *
   * To find a matching method in a class C:&nbsp; If C declares exactly one
   * public method with the specified name and exactly the same formal
   * parameter types, that is the method reflected. If more than one such
   * method is found in C, and one of these methods has a return type that is
   * more specific than any of the others, that method is reflected;
   * otherwise one of the methods is chosen arbitrarily.
   *
   * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.4.
   *
   * @param name the name of the method
   * @param parameterTypes the list of parameters
   * @return the <code>Method</code> object that matches the specified
   * <code>name</code> and <code>parameterTypes</code>
   * @exception NoSuchMethodException if a matching method is not found
   *            or if the name is "&lt;init&gt;"or "&lt;clinit&gt;".
   * @exception NullPointerException if <code>name</code> is <code>null</code>
   * @exception  SecurityException
   *             If a security manager, <i>s</i>, is present and any of the
   *             following conditions is met:
   *
   *             <ul>
   *
   *             <li> invocation of 
   *             <tt>{@link SecurityManager#checkMemberAccess
   *             s.checkMemberAccess(this, Member.PUBLIC)}</tt> denies
   *             access to the method
   *
   *             <li> the caller's class loader is not the same as or an
   *             ancestor of the class loader for the current class and
   *             invocation of <tt>{@link SecurityManager#checkPackageAccess
   *             s.checkPackageAccess()}</tt> denies access to the package
   *             of this class
   *
   *             </ul>
   *
   * @since JDK1.1
   */
  public Method getMethod(String name, Class<?> ... parameterTypes)throws NoSuchMethodException{
  	Type[] types = new Type[parameterTypes.length];
  	int i = 0;
  	for (Class<?> clazz : parameterTypes){
  		types[i] = ClassHelper.AsClass(clazz).getType();
  		i++;
  	}
  	Method result = this.classType.getMethod(name, types);
  	
  	if (result == null)
  		throw new NoSuchMethodException();
  	
  	return result;
  }
  
  
  
  /**
   * Returns an array containing <code>Method</code> objects reflecting all
   * the public <em>member</em> methods of the class or interface represented
   * by this <code>Class</code> object, including those declared by the class
   * or interface and those inherited from superclasses and
   * superinterfaces.  Array classes return all the (public) member methods 
   * inherited from the <code>Object</code> class.  The elements in the array 
   * returned are not sorted and are not in any particular order.  This 
   * method returns an array of length 0 if this <code>Class</code> object
   * represents a class or interface that has no public member methods, or if
   * this <code>Class</code> object represents a primitive type or void.
   *
   * <p> The class initialization method <code>&lt;clinit&gt;</code> is not
   * included in the returned array. If the class declares multiple public
   * member methods with the same parameter types, they are all included in
   * the returned array.
   *
   * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.4.
   *
   * @return the array of <code>Method</code> objects representing the
   * public methods of this class
   * @exception  SecurityException
   *             If a security manager, <i>s</i>, is present and any of the
   *             following conditions is met:
   *
   *             <ul>
   *
   *             <li> invocation of 
   *             <tt>{@link SecurityManager#checkMemberAccess
   *             s.checkMemberAccess(this, Member.PUBLIC)}</tt> denies
   *             access to the methods within this class
   *
   *             <li> the caller's class loader is not the same as or an
   *             ancestor of the class loader for the current class and
   *             invocation of <tt>{@link SecurityManager#checkPackageAccess
   *             s.checkPackageAccess()}</tt> denies access to the package
   *             of this class
   *
   *             </ul>
   *
   * @since JDK1.1
   */
  public Method[] getMethods() {
  	return this.classType.getMethods();
  }
  
  
  
  /**
   * Returns an array containing <code>Field</code> objects reflecting all
   * the accessible public fields of the class or interface represented by
   * this <code>Class</code> object.  The elements in the array returned are
   * not sorted and are not in any particular order.  This method returns an
   * array of length 0 if the class or interface has no accessible public
   * fields, or if it represents an array class, a primitive type, or void.
   *
   * <p> Specifically, if this <code>Class</code> object represents a class,
   * this method returns the public fields of this class and of all its
   * superclasses.  If this <code>Class</code> object represents an
   * interface, this method returns the fields of this interface and of all
   * its superinterfaces.
   *
   * <p> The implicit length field for array class is not reflected by this
   * method. User code should use the methods of class <code>Array</code> to
   * manipulate arrays.
   *
   * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
   *
   * @return the array of <code>Field</code> objects representing the
   * public fields
   * @exception  SecurityException
   *             If a security manager, <i>s</i>, is present and any of the
   *             following conditions is met:
   *
   *             <ul>
   *
   *             <li> invocation of 
   *             <tt>{@link SecurityManager#checkMemberAccess
   *             s.checkMemberAccess(this, Member.PUBLIC)}</tt> denies
   *             access to the fields within this class
   *
   *             <li> the caller's class loader is not the same as or an
   *             ancestor of the class loader for the current class and
   *             invocation of <tt>{@link SecurityManager#checkPackageAccess
   *             s.checkPackageAccess()}</tt> denies access to the package
   *             of this class
   *
   *             </ul>
   *
   * @since JDK1.1
   */
  public Field[] getFields() {
  	return this.classType.getFields();
  }
  
  
  /**
   * Returns a <code>Constructor</code> object that reflects the specified
   * public constructor of the class represented by this <code>Class</code>
   * object. The <code>parameterTypes</code> parameter is an array of
   * <code>Class</code> objects that identify the constructor's formal
   * parameter types, in declared order.  
   *
   * If this <code>Class</code> object represents an inner class
   * declared in a non-static context, the formal parameter types
   * include the explicit enclosing instance as the first parameter.
   *
   * <p> The constructor to reflect is the public constructor of the class
   * represented by this <code>Class</code> object whose formal parameter
   * types match those specified by <code>parameterTypes</code>.
   *
   * @param parameterTypes the parameter array
   * @return the <code>Constructor</code> object of the public constructor that
   * matches the specified <code>parameterTypes</code>
   * @exception NoSuchMethodException if a matching method is not found.
   * @exception  SecurityException
   *             If a security manager, <i>s</i>, is present and any of the
   *             following conditions is met:
   *
   *             <ul>
   *
   *             <li> invocation of 
   *             <tt>{@link SecurityManager#checkMemberAccess
   *             s.checkMemberAccess(this, Member.PUBLIC)}</tt> denies
   *             access to the constructor
   *
   *             <li> the caller's class loader is not the same as or an
   *             ancestor of the class loader for the current class and
   *             invocation of <tt>{@link SecurityManager#checkPackageAccess
   *             s.checkPackageAccess()}</tt> denies access to the package
   *             of this class
   *
   *             </ul>
   *
   * @since JDK1.1
   */
  public Constructor<T> getConstructor(Class<?>... parameterTypes)
      throws NoSuchMethodException {
  	String[] params = new String[parameterTypes.length];
  	int i = 0;
  	for (Class<?> clazz : parameterTypes){
  		params[i] = ClassHelper.AsClass(clazz).getType().getQualifiedSourceName();
  		i++;
  	}
  	
  	Constructor<T> result = this.classType.findConstructor(params);
  	if (result == null){
  		throw new NoSuchMethodException();
  	}
  	
  	return result;
  }
  
  
  
  /**
   * Creates a new instance of the class represented by this <tt>Class</tt>
   * object.  The class is instantiated as if by a <code>new</code>
   * expression with an empty argument list.  The class is initialized if it
   * has not already been initialized.
   *
   * <p>Note that this method propagates any exception thrown by the
   * nullary constructor, including a checked exception.  Use of
   * this method effectively bypasses the compile-time exception
   * checking that would otherwise be performed by the compiler.
   * The {@link
   * java.lang.reflect.Constructor#newInstance(Object...)
   * Constructor.newInstance} method avoids this problem by wrapping
   * any exception thrown by the constructor in a (checked) {@link
   * java.lang.reflect.InvocationTargetException}.
   *
   * @return     a newly allocated instance of the class represented by this
   *             object.
   * @throws Exception 
   * @exception  IllegalAccessException  if the class or its nullary 
   *               constructor is not accessible.
   * @exception  InstantiationException 
   *               if this <code>Class</code> represents an abstract class,
   *               an interface, an array class, a primitive type, or void;
   *               or if the class has no nullary constructor;
   *               or if the instantiation fails for some other reason.
   * @exception  ExceptionInInitializerError if the initialization
   *               provoked by this method fails.
   * @exception  SecurityException
   *             If a security manager, <i>s</i>, is present and any of the
   *             following conditions is met:
   *
   *             <ul>
   *
   *             <li> invocation of 
   *             <tt>{@link SecurityManager#checkMemberAccess
   *             s.checkMemberAccess(this, Member.PUBLIC)}</tt> denies
   *             creation of new instances of this class
   *
   *             <li> the caller's class loader is not the same as or an
   *             ancestor of the class loader for the current class and
   *             invocation of <tt>{@link SecurityManager#checkPackageAccess
   *             s.checkPackageAccess()}</tt> denies access to the package
   *             of this class
   *
   *             </ul>
   *
   */
  public T newInstance() throws Exception {
  	return getConstructor().newInstance();
  }
}
