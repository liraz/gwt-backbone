/*
 * Copyright 2015, Liraz Shilkrot
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.lirazs.gbackbone.generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.lirazs.gbackbone.client.generator.Reflectable;
import org.lirazs.gbackbone.client.generator.Reflection;

public class ReflectionGenerator extends Generator
{
    enum ModelConstructorType {
        ATTRIBUTES_AND_OPTIONS,
        ATTRIBUTES,
        JSON_OBJECT_AND_OPTIONS,
        JSON_OBJECT,
        EMPTY
    }

    @Override
    public String generate( TreeLogger logger, GeneratorContext context, String typeName ) throws UnableToCompleteException
    {
        TypeOracle oracle = context.getTypeOracle( );

        JClassType instantiableType = oracle.findType( Reflectable.class.getName( ) );

        List<JClassType> clazzes = new ArrayList<JClassType>( );

        PropertyOracle propertyOracle = context.getPropertyOracle( );

        for ( JClassType classType : oracle.getTypes( ) )
        {
            if ( !classType.equals( instantiableType ) && classType.isAssignableTo( instantiableType ) )
                clazzes.add( classType );
        }

        final String genPackageName = "org.lirazs.gbackbone.client.generator";
        final String genClassName = "ReflectionImpl";

        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory( genPackageName, genClassName );
        composer.addImplementedInterface( Reflection.class.getCanonicalName( ) );

        composer.addImport( "org.lirazs.gbackbone.client.generator.*" );
        composer.addImport( "org.lirazs.gbackbone.client.core.data.Options" );

        PrintWriter printWriter = context.tryCreate( logger, genPackageName, genClassName );

        if ( printWriter != null )
        {
            SourceWriter sourceWriter = composer.createSourceWriter( context, printWriter );
            sourceWriter.println( "ReflectionImpl( ) {" );
            sourceWriter.println( "}" );

            printFactoryMethod(clazzes, sourceWriter);
            printArrayFactoryMethod( clazzes, sourceWriter );

            sourceWriter.commit( logger );
        }

        return composer.getCreatedClassName( );
    }

    private void printFactoryMethod( List<JClassType> clazzes, SourceWriter sourceWriter )
    {
        sourceWriter.println( );

        sourceWriter.println( "public <T, V extends T> T instantiateModel( Class<V> clazz, Options attributes, Options options ) {" );

        for ( JClassType classType : clazzes )
        {
            if ( classType.isAbstract( ) )
                continue;

            JConstructor[] constructors = classType.getConstructors();
            if (constructors.length > 0) {
                Arrays.sort(constructors, new Comparator<JConstructor>() {
                    @Override
                    public int compare(JConstructor o1, JConstructor o2) {
                        ModelConstructorType modelConstructorType1 = getModelConstructorType(o1);
                        ModelConstructorType modelConstructorType2 = getModelConstructorType(o2);

                        return modelConstructorType1.compareTo(modelConstructorType2);
                    }
                });

                for (JConstructor constructor : constructors) {
                    ModelConstructorType modelConstructorType = getModelConstructorType(constructor);

                    if(modelConstructorType == ModelConstructorType.ATTRIBUTES_AND_OPTIONS) {
                        sourceWriter.println( );
                        sourceWriter.indent( );
                        sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
                        sourceWriter.indent( );
                        sourceWriter.println( "return (T) new " + classType.getQualifiedSourceName( ) + "( attributes, options );" );
                        sourceWriter.outdent( );
                        sourceWriter.println( "}" );
                        sourceWriter.outdent( );
                        sourceWriter.println( );
                    }

                    if(modelConstructorType == ModelConstructorType.ATTRIBUTES) {
                        sourceWriter.println( );
                        sourceWriter.indent( );
                        sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
                        sourceWriter.indent( );
                        sourceWriter.println( "return (T) new " + classType.getQualifiedSourceName( ) + "( attributes );" );
                        sourceWriter.outdent( );
                        sourceWriter.println( "}" );
                        sourceWriter.outdent( );
                        sourceWriter.println( );
                    }

                    if(modelConstructorType == ModelConstructorType.JSON_OBJECT_AND_OPTIONS) {
                        sourceWriter.println( );
                        sourceWriter.indent( );
                        sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
                        sourceWriter.indent( );
                        sourceWriter.println( "return (T) new " + classType.getQualifiedSourceName( ) + "( attributes.toJsonObject(), options );" );
                        sourceWriter.outdent( );
                        sourceWriter.println( "}" );
                        sourceWriter.outdent( );
                        sourceWriter.println( );
                    }

                    if(modelConstructorType == ModelConstructorType.JSON_OBJECT) {
                        sourceWriter.println( );
                        sourceWriter.indent( );
                        sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
                        sourceWriter.indent( );
                        sourceWriter.println( "return (T) new " + classType.getQualifiedSourceName( ) + "( attributes.toJsonObject() );" );
                        sourceWriter.outdent( );
                        sourceWriter.println( "}" );
                        sourceWriter.outdent( );
                        sourceWriter.println( );
                    }

                    if(modelConstructorType == ModelConstructorType.EMPTY) {
                        sourceWriter.println( );
                        sourceWriter.indent( );
                        sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
                        sourceWriter.indent( );
                        sourceWriter.println( "return (T) new " + classType.getQualifiedSourceName( ) + "( );" );
                        sourceWriter.outdent( );
                        sourceWriter.println( "}" );
                        sourceWriter.outdent( );
                        sourceWriter.println( );
                    }
                }
            } else {
                sourceWriter.println( );
                sourceWriter.indent( );
                sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
                sourceWriter.indent( );
                sourceWriter.println( "return (T) new " + classType.getQualifiedSourceName( ) + "( );" );
                sourceWriter.outdent( );
                sourceWriter.println( "}" );
                sourceWriter.outdent( );
                sourceWriter.println( );
            }
        }
        sourceWriter.indent();
        sourceWriter.println("return (T) null;");
        sourceWriter.outdent();
        sourceWriter.println();
        sourceWriter.println("}");
        sourceWriter.outdent( );
        sourceWriter.println();
    }

    private void printArrayFactoryMethod( List<JClassType> clazzes, SourceWriter sourceWriter )
    {
        sourceWriter.println( );

        sourceWriter.println("public <T, V extends T> T[] instantiateArray( Class<V> clazz, int length ) {");

        for ( JClassType classType : clazzes )
        {
            if ( classType.isAbstract( ) )
                continue;

            sourceWriter.println( );
            sourceWriter.indent( );
            sourceWriter.println( "if (clazz.getName().endsWith(\"." + classType.getName( ) + "\")) {" );
            sourceWriter.indent( );
            sourceWriter.println( "return (T[]) new " + classType.getQualifiedSourceName( ) + "[length];" );
            sourceWriter.outdent( );
            sourceWriter.println( "}" );
            sourceWriter.outdent( );
            sourceWriter.println( );
        }
        sourceWriter.indent();
        sourceWriter.println("return (T[]) null;");
        sourceWriter.outdent();
        sourceWriter.println();
        sourceWriter.println("}");
        sourceWriter.outdent( );
        sourceWriter.println( );
    }


    private ModelConstructorType getModelConstructorType(JConstructor constructor) {
        ModelConstructorType modelConstructorType = ModelConstructorType.EMPTY;

        JType[] parameterTypes = constructor.getParameterTypes();

        int optionsParameterCount = 0;
        int jsonObjectParameterCount = 0;

        for (JType parameterType : parameterTypes) {

            if(parameterType.getQualifiedSourceName().equals("org.lirazs.gbackbone.client.core.data.Options"))
                optionsParameterCount++;

            if(parameterType.getQualifiedSourceName().equals("com.google.gwt.json.client.JSONObject"))
                jsonObjectParameterCount++;
        }

        if(optionsParameterCount == 2) {
            modelConstructorType = ModelConstructorType.ATTRIBUTES_AND_OPTIONS;
        } else if(optionsParameterCount == 1) {
            if(jsonObjectParameterCount == 1) {
                modelConstructorType = ModelConstructorType.JSON_OBJECT_AND_OPTIONS;
            } else {
                modelConstructorType = ModelConstructorType.ATTRIBUTES;
            }
        } else if(jsonObjectParameterCount == 1) {
            modelConstructorType = ModelConstructorType.JSON_OBJECT;
        }

        return modelConstructorType;
    }
}
