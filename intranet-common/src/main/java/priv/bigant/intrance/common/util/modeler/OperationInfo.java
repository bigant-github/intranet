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
package priv.bigant.intrance.common.util.modeler;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>Internal configuration information for an <code>Operation</code>
 * descriptor.</p>
 *
 * @author Craig R. McClanahan
 */
public class OperationInfo extends FeatureInfo {

    static final long serialVersionUID = 4418342922072614875L;

    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables

    protected String impact = "UNKNOWN";
    protected final ReadWriteLock parametersLock = new ReentrantReadWriteLock();
    protected ParameterInfo parameters[] = new ParameterInfo[0];


    // ------------------------------------------------------------- Properties

    /**
     * @return the "impact" of this operation, which should be
     *  a (case-insensitive) string value "ACTION", "ACTION_INFO",
     *  "INFO", or "UNKNOWN".
     */
    public String getImpact() {
        return this.impact;
    }


    /**
     * @return the fully qualified Java class name of the return type for this
     * operation.
     */
    public String getReturnType() {
        if(type == null) {
            type = "void";
        }
        return type;
    }

    /**
     * @return the set of parameters for this operation.
     */
    public ParameterInfo[] getSignature() {
        Lock readLock = parametersLock.readLock();
        readLock.lock();
        try {
            return this.parameters;
        } finally {
            readLock.unlock();
        }
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a <code>ModelMBeanOperationInfo</code> object that
     * corresponds to the attribute described by this instance.
     * @return the operation info
     */
    MBeanOperationInfo createOperationInfo() {

        // Return our cached information (if any)
        if (info == null) {
            // Create and return a new information object
            int impact = MBeanOperationInfo.UNKNOWN;
            if ("ACTION".equals(getImpact()))
                impact = MBeanOperationInfo.ACTION;
            else if ("ACTION_INFO".equals(getImpact()))
                impact = MBeanOperationInfo.ACTION_INFO;
            else if ("INFO".equals(getImpact()))
                impact = MBeanOperationInfo.INFO;

            info = new MBeanOperationInfo(getName(), getDescription(),
                                          getMBeanParameterInfo(),
                                          getReturnType(), impact);
        }
        return (MBeanOperationInfo)info;
    }

    protected MBeanParameterInfo[] getMBeanParameterInfo() {
        ParameterInfo params[] = getSignature();
        MBeanParameterInfo parameters[] =
            new MBeanParameterInfo[params.length];
        for (int i = 0; i < params.length; i++)
            parameters[i] = params[i].createParameterInfo();
        return parameters;
    }
}
