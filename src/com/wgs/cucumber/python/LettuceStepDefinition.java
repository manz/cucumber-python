/*
 * Copyright 2013 Web Geo Services
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
package com.wgs.cucumber.python;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emmanuel Peralta
 */
public class LettuceStepDefinition extends AbstractStepDefinition {
    private String myText;
    private List<String> myParameters;

    public LettuceStepDefinition(PyFunction function) {
        super(function);
        myText = extractRegexpFrom(function);
        myParameters = extractParametersNameFrom(function);
    }

    @Override
    public List<String> getVariableNames() {
        return myParameters;
    }

    private List<String> extractParametersNameFrom(PyFunction function) {
        PyParameterList parameterList = function.getParameterList();
        PyParameter params[] = parameterList.getParameters();

        List<String> parametersList = new ArrayList<String>();

        for (int i = 1; i < params.length; i++) {
            PyParameter param = params[i];
            parametersList.add(param.getName());
        }

        return parametersList;
    }

    private String extractRegexpFrom(PyFunction function) {
        String regexp = null;
        PyDecoratorList decorators = function.getDecoratorList();

        if (decorators != null) {
            PyDecorator decorator = decorators.getDecorators()[0];
            PyExpression[] stepArguments = decorator.getArguments();

            regexp = stepArguments[0].getText();

            //TODO: maybe we didn't dig the tree to the String value
            if (regexp.startsWith("u'") || regexp.startsWith("u\"")) {
                regexp = regexp.substring(2, regexp.length() - 1);
            }
        }

        return regexp;
    }

    /** New methods **/
    @Nullable
    @Override
    protected String getCucumberRegexFromElement(PsiElement psiElement) {
        if (psiElement instanceof PyFunction) {
            return extractRegexpFrom((PyFunction)psiElement);
        }
        return "";
    }
}
