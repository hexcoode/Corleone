/*
 * Copyright (C) 2015 Jorge Castillo Pérez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jorgecastilloprz.corleone;

import com.github.jorgecastilloprz.corleone.internal.ParamBinder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.Modifier;

/**
 * This class wraps all the logic for generating a ParamBinder java source file. It is using
 * Square's JavaPoet for source generation.
 *
 * @author Jorge Castillo Pérez
 */
class ParamBinderGenerator {

  static final String SEPARATOR = "$$";
  static final String SUFFIX = "ParamBinder";

  private JobDataModel jobDataModel;

  ParamBinderGenerator(JobDataModel jobDataModel) {
    this.jobDataModel = jobDataModel;
  }

  JavaFile generate() {
    TypeSpec paramBinder = TypeSpec.classBuilder(getBinderClassName())
        .addModifiers(Modifier.FINAL)
        .addSuperinterface(getInterfaceToImplement())
        .addMethod(generateBindParamsMethod())
        .build();

    JavaFile javaFile = JavaFile.builder(jobDataModel.getPackageName(), paramBinder).build();
    return javaFile;
  }

  private MethodSpec generateBindParamsMethod() {
    MethodSpec.Builder bindParamsMethod = MethodSpec.methodBuilder("bindParams")
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(ClassName.get(jobDataModel.getClassType()), "target");

    for (ParamFieldDataModel paramField : jobDataModel.getParams()) {
      bindParamsMethod.addStatement("target."
              + paramField.getName()
              + " = ("
              + paramField.getType()
              + ") $T.getParamsValueForQualifierAndContext($S,$S)", ParamBinderHelper.class,
          paramField.getQualifier(), jobDataModel.getContext());
    }

    return bindParamsMethod.build();
  }

  private ParameterizedTypeName getInterfaceToImplement() {
    return ParameterizedTypeName.get(ClassName.get(ParamBinder.class),
        TypeVariableName.get(jobDataModel.getClassName()));
  }

  private String getBinderClassName() {
    return jobDataModel.getClassName() + SEPARATOR + jobDataModel.getContext() + SEPARATOR + SUFFIX;
  }

  static String getBinderClassNameForClassAndContext(String classSimpleName, String context) {
    return classSimpleName + SEPARATOR + context + SEPARATOR + SUFFIX;
  }
}