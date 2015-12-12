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


package org.lirazs.gbackbone.gen.reflection.accessadapter;

import com.google.gwt.core.ext.typeinfo.JMethod;
import org.lirazs.gbackbone.reflection.client.AccessDef;

public class JMethodAdapter implements AccessDef{

	private JMethod method;
	
	public JMethodAdapter(JMethod method){
		this.method = method;
	}
	
	public boolean isFinal() {
		return method.isFinal();
	}

	public boolean isPrivate() {
		return method.isPrivate();
	}

	public boolean isProtected() {
		return method.isProtected();
	}

	public boolean isPublic() {
		return method.isPublic();
	}

	public boolean isStatic() {
		return method.isStatic();
	}

}
