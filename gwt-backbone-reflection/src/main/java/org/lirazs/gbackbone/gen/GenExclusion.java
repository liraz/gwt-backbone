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


package org.lirazs.gbackbone.gen;

import com.google.gwt.core.ext.typeinfo.JClassType;

public interface GenExclusion {
	/**
	 * Used for Generator system
	 * Sometimes, You don't want generate all stuff which meet the conditions
	 * 
	 * for example
	 * AOP just apply to Class, not interface, so we can implement a new GenExclustion
	 * tell generator system don't generate source code for interface
	 * 
	 * @param classType
	 * @return
	 */
	boolean exclude(JClassType classType);
}
