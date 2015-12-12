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


package org.lirazs.gbackbone.reflection.client;

import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_BOOLEAN;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_BYTE;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_CHAR;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_DOUBLE;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_FLOAT;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_INT;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_LONG;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_SHORT;
import static org.lirazs.gbackbone.reflection.client.JniConstants.DESC_VOID;

import org.lirazs.gbackbone.reflection.client.impl.PrimitiveTypeImpl;

public interface PrimitiveType extends Type {

  public static final PrimitiveType BOOLEAN = PrimitiveTypeImpl.create("boolean", "Boolean", DESC_BOOLEAN);
  public static final PrimitiveType BYTE = PrimitiveTypeImpl.create("byte", "Byte", DESC_BYTE);
  public static final PrimitiveType CHAR = PrimitiveTypeImpl.create("char", "Character", DESC_CHAR);
  public static final PrimitiveType DOUBLE = PrimitiveTypeImpl.create("double", "Double", DESC_DOUBLE);
  public static final PrimitiveType FLOAT = PrimitiveTypeImpl.create("float", "Float", DESC_FLOAT);
  public static final PrimitiveType INT = PrimitiveTypeImpl.create("int", "Integer", DESC_INT);
  public static final PrimitiveType LONG = PrimitiveTypeImpl.create("long", "Long", DESC_LONG);
  public static final PrimitiveType SHORT = PrimitiveTypeImpl.create("short", "Short", DESC_SHORT);
  public static final PrimitiveType VOID = PrimitiveTypeImpl.create("void", "Void", DESC_VOID);

  public String getQualifiedBoxedSourceName();
  
}