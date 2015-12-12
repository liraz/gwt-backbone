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
 * FileName: AnnotationStoreImpl.java
 * Author:		JamesLuo.au@gmail.com
 * purpose:
 * 
 * History:
 * 
 */


package org.lirazs.gbackbone.reflection.client;

import java.lang.annotation.Annotation;
import java.util.Map;

public class AnnotationStoreImpl implements Annotation {

  private final Class<? extends Annotation> clasz;
  private final Map<String, String> values;
  
  public AnnotationStoreImpl(Class<? extends Annotation> clasz, Map<String, String> values){
    this.clasz = clasz;
    this.values = values;
  }
  
  public Class<? extends Annotation> annotationType() {
    return clasz;
  }

  /**
   * Just support string value for now, all other type is a string here(object.toString())
   */
  public String getValue(String name) {
    return values.get(name);
  }

  public Map<String, String> allValues() {
    return values;
  }

	public ClassType getAsClassType(String name) throws ReflectionRequiredException {
		String value = getValue(name);
		return TypeOracle.Instance.getClassType(value);
	}

	public String[] getAsStringArray(String name) {
		String value = getValue(name).trim();
		if ((value.startsWith("[")) && (value.endsWith("]"))){
			value = value.substring(1, value.length() - 1);
			return value.split(",");
		}
		
		return null;
	}

}
