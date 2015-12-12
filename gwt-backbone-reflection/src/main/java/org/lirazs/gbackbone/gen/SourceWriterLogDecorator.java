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

import java.util.Stack;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

public class SourceWriterLogDecorator implements SourceWriter {

	private SourceWriter sourceWriter;
	private TreeLogger rootLogger;
	private boolean useLog;
	
	private TreeLogger currentTreeLogger = null;
	private final Stack/*<TreeLogger>*/ logStack = new Stack/*<TreeLogger>*/();
	
	private int lineNumber = 0;
	
	private String lineCache = "";
	
	//private final TreeLogger.Type logType = TreeLogger.ERROR;
	private final TreeLogger.Type logType = TreeLogger.DEBUG;
	
	public SourceWriterLogDecorator(SourceWriter sourceWriter, TreeLogger logger, boolean useLog, int baseLineNumber){
		this.sourceWriter = sourceWriter;
		this.rootLogger = logger;
		this.lineNumber = baseLineNumber;
		logStack.push(rootLogger);
		currentTreeLogger = rootLogger.branch(logType, "branch for new source", null);
		logStack.push(currentTreeLogger);

		this.useLog = useLog;
	}
	
	public void beginJavaDocComment() {
		sourceWriter.beginJavaDocComment();
	}

	public void commit(TreeLogger logger) {
		sourceWriter.commit(logger);
		
	}

	public void endJavaDocComment() {
		sourceWriter.endJavaDocComment();
		
	}

	public void indent() {
		sourceWriter.indent();
		
		if (useLog){
			push(currentTreeLogger);
			currentTreeLogger = currentTreeLogger.branch(logType, "", null);
		}
		
	}

	public void indentln(String s) {
		sourceWriter.indentln(s);
		
		if (useLog){
			lineNumber++;
			push(currentTreeLogger.branch(logType, "" + s, null));
		}
		
	}

	public void outdent() {
		sourceWriter.outdent();

		if (useLog){
			//pop().log(logType, "(outdent)", null);
			pop();
		}
	}

	public void print(String s) {
		//TODO this have a mistake, wrong line with log!
		sourceWriter.print(s);
		if (useLog){
			log(s, false);
		}
		
	}

	public void println() {
		sourceWriter.println();
		
		if (useLog){
			lineNumber++;
			log("", true);
		}	
	}

	public void println(String s) {
		sourceWriter.println(s);
		
		if (useLog){
			lineNumber++;
			log(s, true);
		}	
	}
	

	/**
	 * pop TreeLogger to currentTreeLogger;
	 * @return
	 */
	private TreeLogger pop(){
		if (logStack.empty()){
			currentTreeLogger = null;
		}else{
			currentTreeLogger = (TreeLogger)this.logStack.pop();
		}
		
		
		/**
		 * we use rootlogger as the last logger, this can tall use the dent is error.
		 */
		if (currentTreeLogger == null){
			currentTreeLogger = this.rootLogger;
		}
		
		return currentTreeLogger;
	}
	
	/**
	 * push to stack and currnetTreeLogger
	 * @param logger
	 */
	private void push(TreeLogger logger){
		this.logStack.push(logger);
		this.currentTreeLogger = logger;
	}
	
	private void log(String msg, boolean newLine){
		if (newLine){
			currentTreeLogger.log(logType, lineCache + msg + "(" + lineNumber + ")", null);
			lineCache = "";
		}else{
			lineCache = lineCache + msg;
		}
		
	}

	public void indentln(String s, Object... args) {
		// TODO Auto-generated method stub
		
	}

	public void print(String s, Object... args) {
		// TODO Auto-generated method stub
		
	}

	public void println(String s, Object... args) {
		// TODO Auto-generated method stub
		
	}

}
