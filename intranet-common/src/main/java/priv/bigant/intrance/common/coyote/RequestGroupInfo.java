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

import java.util.ArrayList;

/**
 * This can be moved to top level ( eventually with a better name ). It is currently used only as a JMX artifact, to
 * aggregate the data collected from each RequestProcessor thread.
 */
public class RequestGroupInfo {
    private final ArrayList<RequestInfo> processors = new ArrayList<>();
    private long deadMaxTime = 0;


    public synchronized void addRequestProcessor(RequestInfo rp) {
        processors.add(rp);
    }

    public synchronized void removeRequestProcessor(RequestInfo rp) {
        if (rp != null) {
            if (deadMaxTime < rp.getMaxTime())
                deadMaxTime = rp.getMaxTime();

            processors.remove(rp);
        }
    }


}
