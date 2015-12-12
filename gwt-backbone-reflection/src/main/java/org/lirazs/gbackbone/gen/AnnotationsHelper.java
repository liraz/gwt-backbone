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

/**
 * FileName: JRealClassTypeHelper.java
 * Author:		JamesLuo.au@gmail.com
 * purpose:
 * 
 * History:
 * 
 */


package org.lirazs.gbackbone.gen;

import com.google.gwt.core.ext.typeinfo.*;

import java.lang.annotation.Annotation;

public class AnnotationsHelper {
  public static Annotation[] getAnnotations(HasAnnotations annotations){
    if (annotations instanceof JClassType)
      return getAnnotations((JClassType)annotations);
    else if (annotations instanceof JAbstractMethod)
      return getAnnotations((JAbstractMethod)annotations);
    else if (annotations instanceof JField)
      return getAnnotations((JField)annotations);
    else if (annotations instanceof JPackage)
      return getAnnotations((JPackage)annotations);
    else if (annotations instanceof JParameter)
      return getAnnotations((JParameter)annotations);
    else
      return null;
  }
  
  public static Annotation[] getAnnotations(JClassType classType){
    return classType.getAnnotations();
  }
  
  public static Annotation[] getAnnotations(JAbstractMethod type){
    return type.getAnnotations();
  }
  
  public static Annotation[] getAnnotations(JField type){
    return type.getAnnotations();
  }
  
  public static Annotation[] getAnnotations(JPackage type){
    return type.getAnnotations();
  }
  
  public static Annotation[] getAnnotations(JParameter type){
    return type.getAnnotations();
  }
  
}
