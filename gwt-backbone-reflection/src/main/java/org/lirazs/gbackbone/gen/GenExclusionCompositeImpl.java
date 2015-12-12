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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.JClassType;

public class GenExclusionCompositeImpl implements GenExclusion, GenExclusionComposite {

	private List<GenExclusion> genExclusions = new ArrayList<GenExclusion>();
	
	public void addGenExclusion(GenExclusion exclusion){
		genExclusions.add(exclusion);
	}
	
	public boolean exclude(JClassType classType) {
		for (GenExclusion e : genExclusions){
			if (e.exclude(classType))
				return true;
		}
		
		return false;
	}

}
