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
package priv.bigant.intrance.common.coyote.http11.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.coyote.UpgradeToken;
import priv.bigant.intrance.common.coyote.http11.servlet.ServletInputStream;
import priv.bigant.intrance.common.util.net.AbstractEndpoint;
import priv.bigant.intrance.common.util.net.SSLSupport;
import priv.bigant.intrance.common.util.net.SocketEvent;
import priv.bigant.intrance.common.util.net.SocketWrapperBase;

import java.io.IOException;

public class UpgradeProcessorInternal extends UpgradeProcessorBase {

    private static final Logger log = LoggerFactory.getLogger(UpgradeProcessorInternal.class);

    private final InternalHttpUpgradeHandler internalHttpUpgradeHandler;

    public UpgradeProcessorInternal(SocketWrapperBase<?> wrapper,
                                    UpgradeToken upgradeToken) {
        super(upgradeToken);
        this.internalHttpUpgradeHandler = (InternalHttpUpgradeHandler) upgradeToken.getHttpUpgradeHandler();
        /*
         * Leave timeouts in the hands of the upgraded protocol.
         */
        wrapper.setReadTimeout(INFINITE_TIMEOUT);
        wrapper.setWriteTimeout(INFINITE_TIMEOUT);

        internalHttpUpgradeHandler.setSocketWrapper(wrapper);
    }


    @Override
    public AbstractEndpoint.Handler.SocketState dispatch(SocketEvent status) {
        return internalHttpUpgradeHandler.upgradeDispatch(status);
    }


    @Override
    public final void setSslSupport(SSLSupport sslSupport) {
        internalHttpUpgradeHandler.setSslSupport(sslSupport);
    }


    @Override
    public void pause() {
        internalHttpUpgradeHandler.pause();
    }


    @Override
    protected Logger getLog() {
        return log;
    }


    // --------------------------------------------------- AutoCloseable methods

    @Override
    public void close() throws Exception {
        internalHttpUpgradeHandler.destroy();
    }


    // --------------------------------------------------- WebConnection methods

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

}
