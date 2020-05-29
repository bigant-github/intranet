/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package priv.bigant.intrance.common.coyote;

import javax.management.ObjectName;


/**
 * Structure holding the Request and Response objects. It also holds statistical informations about request processing
 * and provide management informations about the requests being processed.
 * <p>
 * Each thread uses a Request/Response pair that is recycled on each request. This object provides a place to collect
 * global low-level statistics - without having to deal with synchronization ( since each thread will have it's own
 * RequestProcessorMX ).
 *
 * @author Costin Manolache
 */
public class RequestInfo {
    private RequestGroupInfo global = null;

    // ----------------------------------------------------------- Constructors

    public RequestInfo(Request req) {
        this.req = req;
    }

    public void setGlobalProcessor(RequestGroupInfo global) {
        if (global != null) {
            this.global = global;
            global.addRequestProcessor(this);
        } else {
            if (this.global != null) {
                this.global.removeRequestProcessor(this);
                this.global = null;
            }
        }
    }


    // ----------------------------------------------------- Instance Variables
    private final Request req;
    private ObjectName rpName;

    // -------------------- Information about the current request  -----------
    // This is useful for long-running requests only

    public String getMethod() {
        return req.method().toString();
    }

    public String getProtocol() {
        return req.protocol().toString();
    }


    public int getContentLength() {
        return req.getContentLength();
    }

    private long maxTime;


    /**
     * Called by the processor before recycling the request. It'll collect statistic information.
     */
    void updateCounters() {
        //TODO bytesSent+=req.getResponse().getContentWritten();

        //TODO if( req.getResponse().getStatus() >=400 )
        //TODO    errorCount++;
        long t0 = req.getStartTime();
        long t1 = System.currentTimeMillis();
        long time = t1 - t0;
        if (maxTime < time) {
            maxTime = time;
        }
    }


    long getMaxTime() {
        return maxTime;
    }

    public ObjectName getRpName() {
        return rpName;
    }

    public void setRpName(ObjectName rpName) {
        this.rpName = rpName;
    }

}
