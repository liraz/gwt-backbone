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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * usage:
 * 
 *   inherited this class and override doGetSourceWriter abstract method
 *   use useLog to set if have log output
 *   
 * @author James Luo
 * 2007-12-20 12:21:43 am
 *
 */
public abstract class LogableSourceCreator {

	private boolean useLog = false;
	private SourceWriter sourceWriter = null; 
	
	protected TreeLogger logger;
	protected GeneratorContext context;
	protected TypeOracle typeOracle;
	protected String typeName;

	public LogableSourceCreator(TreeLogger logger, GeneratorContext context,
			String typeName) {
		this.logger = logger;
		this.context = context;
		this.typeOracle = context.getTypeOracle();
		this.typeName = typeName;
	}

	public boolean isUseLog() {
		return useLog;
	}

	public void setUseLog(boolean useLog) {
		this.useLog = useLog;
		
		if (logger == null) this.useLog = false;
	}
	
	/**
	 * override this method to provide really SourceWriter
	 * getSourceWriter will return the Decoratored SourceWriter;
	 * @param classType
	 * @return
	 */
	protected abstract SourceWriter doGetSourceWriter(JClassType classType) throws Exception;
	protected abstract String getSUFFIX();
	protected abstract void createSource(SourceWriter source, JClassType classType);
	
	protected GenExclusion getGenExclusion(){
		return null;
	}
	
	protected boolean genExclusion(JClassType classType){
		if (getGenExclusion() != null){
			return getGenExclusion().exclude(classType);
		}else
			return false;
	}
	
	
	public String generate() throws UnableToCompleteException{
		JClassType classType;
		try {
			logger.log(Type.DEBUG, "Start generate UNIT for " + typeName + " in " + this.getClass().getName());
			Calendar start = Calendar.getInstance();
			
			classType = typeOracle.getType(typeName);
			if (genExclusion(classType)){
				return null;
			}
		
			SourceWriter source = getSourceWriter(classType, isUseLog(), 6);
	
			if ((source != null)) {
				source.indent();
				createSource(source, classType);
				source.outdent();
				source.commit(logger);
			}
			
			logger.log(Type.DEBUG, "Code commited, Unit name: " + getUnitName(classType) + " Time:" + (start.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()));
			
			return getUnitName(classType);
		} catch (Throwable e) {
			this.logger.log(Type.ERROR, e.getMessage(), e);
			throw new UnableToCompleteException();
		}
	}
	
	/**
	 * return the Decorator of SourceWriter
	 * @param classType
	 * @param useLog
	 * @param baseLineNumber
	 * @return
	 * @throws Exception 
	 */
	public SourceWriter getSourceWriter(JClassType classType, boolean useLog, int baseLineNumber) throws Exception{
		if ((sourceWriter == null) && (getUnitName(classType) != null)){
			sourceWriter = doGetSourceWriter(classType);
			//Decorator it
			if (sourceWriter != null)
				sourceWriter = new SourceWriterLogDecorator(sourceWriter, this.logger, useLog, baseLineNumber);
			//else
			//	throw new CheckedExceptionWrapper("Can't create Source Writer, please make sure there is no same class in your source folder.");
		}
		
		return sourceWriter;
	}
	
	protected String getPackageName(JClassType classType){
		return classType.getPackage().getName();
	}
	
	/**
	 * this name will return to GWT compiler
	 * if the class you don't care in this creator, just return the original QualifiedSourceName
	 * @param classType
	 * @return
	 */
	protected String getUnitName(JClassType classType){    
	    String packageName = getPackageName(classType);
	    String className = getSimpleUnitName(classType);
	    
		//return classType.getParameterizedQualifiedSourceName() + SUFFIX;
	    return packageName + "." + className;
	}
	
	
	protected String getSimpleUnitName(JClassType classType){
		return getSimpleUnitNameWithOutSuffix(classType) + getSUFFIX();
	}
	
	protected String getSimpleUnitNameWithOutSuffix(JClassType classType){
		return classType.getName().replace('.', '_');
	}
	
	

}
