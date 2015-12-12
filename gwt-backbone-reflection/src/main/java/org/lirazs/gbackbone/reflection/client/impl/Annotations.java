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

package org.lirazs.gbackbone.reflection.client.impl;

import org.lirazs.gbackbone.reflection.client.ClassHelper;
import org.lirazs.gbackbone.reflection.client.HasAnnotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Default implementation of the {@link HasAnnotations} interface.
 */
class Annotations implements HasAnnotations {
	/**
	 * All annotations declared on the annotated element.
	 */
	private Map<Class<?>, Annotation> declaredAnnotations = new HashMap<Class<?>, Annotation>();

	/**
	 * Lazily initialized collection of annotations declared on or inherited by
	 * the annotated element.
	 */
	private Map<Class<?>, Annotation> lazyAnnotations = null;


	/**
	 * If not <code>null</code> the parent to inherit annotations from.
	 */
	private HasAnnotations parent;

	Annotations() {
	}

	Annotations(Map<Class<? extends Annotation>, Annotation> declaredAnnotations) {
		this.declaredAnnotations.putAll(declaredAnnotations);
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		initializeAnnotations();
		return (T) lazyAnnotations.get(annotationClass);
	}

	public Annotation[] getAnnotations() {
		initializeAnnotations();
		Collection<Annotation> values = lazyAnnotations.values();
		return values.toArray(new Annotation[values.size()]);
	}

	public Annotation[] getDeclaredAnnotations() {
		initializeAnnotations();
		Collection<Annotation> values = declaredAnnotations.values();
		return values.toArray(new Annotation[values.size()]);
	}

	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}

	public void addAnnotation(Annotation ann) {
		if (ann != null)
			this.declaredAnnotations.put(ann.annotationType(), ann);
	}
	

	void setParent(HasAnnotations parent) {
		this.parent = parent;
	}

	private void initializeAnnotations() {
		if (lazyAnnotations != null) {
			return;
		}

		if (parent != null) {
			lazyAnnotations = new HashMap<Class<?>, Annotation>();
			// ((Annotations)parent).initializeAnnotations();
			// for (Entry<Class<?>, Annotation> entry :
			// ((Annotations)parent).lazyAnnotations.entrySet()) {
			// if
			// (entry.getValue().annotationType().isAnnotationPresent(Inherited.class))
			// {
			// lazyAnnotations.put(entry.getKey(), entry.getValue());
			// }
			// }

			for (Annotation a : parent.getAnnotations()) {
				if (ClassHelper.AsClass(a.annotationType())
						.isAnnotationPresent(Inherited.class)) {
					lazyAnnotations.put(a.annotationType(), a);
				}
			}

			lazyAnnotations.putAll(declaredAnnotations);
		} else {
			lazyAnnotations = declaredAnnotations;
		}
	}
}
