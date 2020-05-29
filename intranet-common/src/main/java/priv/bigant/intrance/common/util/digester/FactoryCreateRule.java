/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package priv.bigant.intrance.common.util.digester;

import org.xml.sax.Attributes;


/**
 * a new object which it pushes onto the object stack.  When the element is
 * complete, the object will be popped.</p>
 *
 * <p>This rule is intended in situations where the element's attributes are
 * needed before the object can be created.  A common scenario is for the
 * ObjectCreationFactory implementation to use the attributes  as parameters
 * in a call to either a factory method or to a non-empty constructor.
 */

public class FactoryCreateRule extends Rule {

    // ----------------------------------------------------------- Fields

    /** Should exceptions thrown by the factory be ignored? */
    private boolean ignoreCreateExceptions;
    /** Stock to manage */
    private ArrayStack<Boolean> exceptionIgnoredStack;


    // ----------------------------------------------------------- Constructors



    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    /**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {

        if (ignoreCreateExceptions) {

            if (exceptionIgnoredStack == null) {
                exceptionIgnoredStack = new ArrayStack<>();
            }

            try {

                exceptionIgnoredStack.push(Boolean.FALSE);

            } catch (Exception e) {
                // log message and error
                if (digester.log.isInfoEnabled()) {
                    digester.log.info("[FactoryCreateRule] Create exception ignored: " +
                        ((e.getMessage() == null) ? e.getClass().getName() : e.getMessage()));
                    if (digester.log.isDebugEnabled()) {
                        digester.log.debug("[FactoryCreateRule] Ignored exception:", e);
                    }
                }
                exceptionIgnoredStack.push(Boolean.TRUE);
            }

        } else {

        }
    }


    /**
     * Process the end of this element.
     */
    @Override
    public void end(String namespace, String name) throws Exception {

        // check if object was created
        // this only happens if an exception was thrown and we're ignoring them
        if (
                ignoreCreateExceptions &&
                exceptionIgnoredStack != null &&
                !(exceptionIgnoredStack.empty())) {

            if ((exceptionIgnoredStack.pop()).booleanValue()) {
                // creation exception was ignored
                // nothing was put onto the stack
                if (digester.log.isTraceEnabled()) {
                    digester.log.trace("[FactoryCreateRule] No creation so no push so no pop");
                }
                return;
            }
        }

        Object top = digester.pop();
        if (digester.log.isDebugEnabled()) {
            digester.log.debug("[FactoryCreateRule]{" + digester.match +
                    "} Pop " + top.getClass().getName());
        }

    }


    /**
     * Render a printable version of this Rule.
     */
    @Override
    public String toString() {
        return ("FactoryCreateRule[" + "]");

    }
}
