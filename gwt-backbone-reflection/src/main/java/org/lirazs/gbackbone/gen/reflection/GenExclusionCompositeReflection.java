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

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.lirazs.gbackbone.gen.GenExclusion;
import org.lirazs.gbackbone.gen.GenExclusionCompositeImpl;
import org.lirazs.gbackbone.gen.GenUtils;

/**
 * Need found a way to set exclusion from module xml file.
 * For now we hard code here.
 * 
 * @author JamesLuo.au@gmail.com
 *
 */
public class GenExclusionCompositeReflection extends GenExclusionCompositeImpl implements GenExclusion {
	private GenExclusionCompositeReflection(){
		//Can don't create reflection for AOP generated source.
		addGenExclusion(new GenExclusion(){

			public boolean exclude(JClassType classType) {
				String qualifiedSourceName = classType.getQualifiedSourceName();

				if (classType.getName().endsWith(GenUtils.getAOP_SUFFIX())) {
					return true;
				}

				//no need for system or gwt core classes
				if (qualifiedSourceName.contains("java.lang") || qualifiedSourceName.contains("java.util")
						|| qualifiedSourceName.contains("java.io")
						|| qualifiedSourceName.contains("com.google.gwt"))
					return true;

				return false;
			}
			
		});
	}
	
	public static final GenExclusionCompositeReflection INSTANCE = new GenExclusionCompositeReflection();
}
