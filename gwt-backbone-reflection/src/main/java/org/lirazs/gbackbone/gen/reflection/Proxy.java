/*******************************************************************************
 *  Copyright 2001, 2007 JamesLuo(JamesLuo.au@gmail.com)
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 * 
 *  Contributors:
 *******************************************************************************/


package org.lirazs.gbackbone.gen.reflection;

import org.lirazs.gbackbone.reflection.client.MethodInvokeException;
import org.lirazs.gbackbone.reflection.client.impl.ClassTypeImpl;
import org.lirazs.gbackbone.reflection.client.impl.ConstructorImpl;
import org.lirazs.gbackbone.reflection.client.impl.FieldImpl;
import org.lirazs.gbackbone.reflection.client.impl.MethodImpl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Proxy extends ClassTypeImpl<Proxy> {
	

	public Proxy(){
		super(Proxy.class);
		addFields();
		addMethods();
		
		new ConstructorImpl(this){
			public Object newInstance() {
				//return GWT.create(XXX.class);
				return null;
			}
			
		};
	}

	protected Map<Class<? extends Annotation>, Annotation> AnnotationsArrayToMaps(Annotation[] annotations){
	  Map<Class<? extends Annotation>, Annotation> result = new HashMap<Class<? extends Annotation>, Annotation>();
	  for (Annotation annotation : annotations) {
     result.put(annotation.getClass(), annotation); 
    }
	  return result;
	}
	
	public void addAnnotations(){
	  
	}
	
	protected void checkInvokeParams(String methodName, int paramCount, Object[] args) throws IllegalArgumentException{
		if (args.length != paramCount){
			throw new IllegalArgumentException("Method: " + methodName + "request " + paramCount + " params, but invoke provide " + args.length + " params.");
		} 
	}
	
	
	public Object invoke(Object instance, String methodName, Object[] args) throws MethodInvokeException {
		Object content = instance;
		if (methodName.equals("method1")) {
			checkInvokeParams(methodName, 3, args);
			return ((Object)content).toString();
		}else{
			if (methodName.equals("method1")) {
				checkInvokeParams(methodName, 3, args);
				return ((Object)content).toString();
			}else if (methodName.equals("method2")) {
				checkInvokeParams(methodName, 1, args);
				return ((List<?>)content).toArray((Object[])args[0]);
			}else{
				return super.invoke(instance, methodName, args);
				//throw new IllegalArgumentException("Method: " + methodName + " can't found.");
			}
		}
	}
	
	
	protected void addFields(){
		//for...
		FieldImpl field = new FieldImpl(this, "name");
		//field.addModifierBits(0);   
		//field.addMetaData(tagName, values);  //for... every meta
		//field.addMetaData("tag", new String[]{"meta1", "meta2"});
		field.setTypeName("typeName");
		addField(field);
	}
	
	protected void addMethods(){
		//for...
		MethodImpl method = new MethodImpl(this, "name");
		//method.addModifierBits(bits);
		//method.addMetaData(tagName, values)
		method.setReturnTypeName("returnTypeName");
		//JParameter jparam = null;
		//new Parameter(method, jparam.getType().getQualifiedSourceName(), jparam.getName());
	}
	
	
	protected void addAnnotation(Annotation[] annotations){
	  Map<String, Object> values = new HashMap<String, Object>();
	  values.put("name", "Test_Table");
	  //AnnotationStoreImpl store = new AnnotationStoreImpl(org.lirazs.gbackbone.test.TestAnnotation.class, values);
	}
}