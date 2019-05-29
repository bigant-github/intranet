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
package priv.bigant.intrance.common.util.net.openssl;

import priv.bigant.intrance.common.util.net.SSLHostConfigCertificate;
import priv.bigant.intrance.common.util.net.SSLImplementation;
import priv.bigant.intrance.common.util.net.SSLSupport;
import priv.bigant.intrance.common.util.net.SSLUtil;
import priv.bigant.intrance.common.util.net.jsse.JSSESupport;

import javax.net.ssl.SSLSession;


public class OpenSSLImplementation extends SSLImplementation {

    @Override
    public SSLSupport getSSLSupport(SSLSession session) {
        return new JSSESupport(session);
    }

    @Override
    public SSLUtil getSSLUtil(SSLHostConfigCertificate certificate) {
        return new OpenSSLUtil(certificate);
    }

    @Override
    public boolean isAlpnSupported() {
        // OpenSSL supported ALPN
        return true;
    }
}
