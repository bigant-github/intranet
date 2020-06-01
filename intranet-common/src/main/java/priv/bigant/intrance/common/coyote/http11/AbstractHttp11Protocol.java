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
package priv.bigant.intrance.common.coyote.http11;

import priv.bigant.intrance.common.coyote.*;


public abstract class AbstractHttp11Protocol<S> extends AbstractProtocol<S> {

    private final CompressionConfig compressionConfig = new CompressionConfig();


    /**
     * @return See {@link #getCompressibleMimeType()}
     * @deprecated Use {@link #getCompressibleMimeType()}
     */
    @Deprecated
    public String getCompressableMimeType() {
        return getCompressibleMimeType();
    }

    /**
     * @param valueS See {@link #setCompressibleMimeType(String)}
     * @deprecated Use {@link #setCompressibleMimeType(String)}
     */
    @Deprecated
    public void setCompressableMimeType(String valueS) {
        setCompressibleMimeType(valueS);
    }

    /**
     * @return See {@link #getCompressibleMimeTypes()}
     * @deprecated Use {@link #getCompressibleMimeTypes()}
     */
    @Deprecated
    public String[] getCompressableMimeTypes() {
        return getCompressibleMimeTypes();
    }


    public String getCompressibleMimeType() {
        return compressionConfig.getCompressibleMimeType();
    }

    public void setCompressibleMimeType(String valueS) {
        compressionConfig.setCompressibleMimeType(valueS);
    }

    public String[] getCompressibleMimeTypes() {
        return compressionConfig.getCompressibleMimeTypes();
    }


    // ------------------------------------------------ HTTP specific properties
    // ------------------------------------------ passed through to the EndPoint

    // ----------------------------------------------- HTTPS specific properties
    // ------------------------------------------ passed through to the EndPoint


    // ------------------------------------------------------------- Common code


}
