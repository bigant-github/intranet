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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.net.SSLContext;
import priv.bigant.intrance.common.util.net.SSLHostConfigCertificate;
import priv.bigant.intrance.common.util.net.SSLUtilBase;
import priv.bigant.intrance.common.util.net.jsse.JSSEKeyManager;
import priv.bigant.intrance.common.util.res.StringManager;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.List;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;


public class OpenSSLUtil extends SSLUtilBase {

    private static final Logger log = LoggerFactory.getLogger(OpenSSLUtil.class);
    private static final StringManager sm = StringManager.getManager(OpenSSLContext.class);


    public OpenSSLUtil(SSLHostConfigCertificate certificate) {
        super(certificate);
    }


    @Override
    protected Logger getLog() {
        return log;
    }


    @Override
    protected Set<String> getImplementedProtocols() {
        return OpenSSLEngine.IMPLEMENTED_PROTOCOLS_SET;
    }


    @Override
    protected Set<String> getImplementedCiphers() {
        return OpenSSLEngine.AVAILABLE_CIPHER_SUITES;
    }


    @Override
    protected boolean isTls13RenegAuthAvailable() {
        // OpenSSL does support authentication after the initial handshake
        return true;
    }


    @Override
    public SSLContext createSSLContextInternal(List<String> negotiableProtocols) throws Exception {
        return new OpenSSLContext(certificate, negotiableProtocols);
    }


    public static X509KeyManager chooseKeyManager(KeyManager[] managers) throws Exception {
        if (managers == null) {
            return null;
        }
        for (KeyManager manager : managers) {
            if (manager instanceof JSSEKeyManager) {
                return (JSSEKeyManager) manager;
            }
        }
        for (KeyManager manager : managers) {
            if (manager instanceof X509KeyManager) {
                return (X509KeyManager) manager;
            }
        }
        throw new IllegalStateException(sm.getString("openssl.keyManagerMissing"));
    }


    @Override
    public KeyManager[] getKeyManagers() throws Exception {
        try {
            return super.getKeyManagers();
        } catch (KeyStoreException | IOException e) {
            // Depending on what is presented, JSSE may throw either of the
            // above exceptions if it doesn't understand the provided file.
            if (certificate.getCertificateFile() != null) {
                if (log.isDebugEnabled()) {
                    log.info(sm.getString("openssl.nonJsseCertficate",
                            certificate.getCertificateFile(), certificate.getCertificateKeyFile()), e);
                } else {
                    log.info(sm.getString("openssl.nonJsseCertficate",
                            certificate.getCertificateFile(), certificate.getCertificateKeyFile()));
                }
                // Assume JSSE processing of the certificate failed, try again with OpenSSL
                // without a key manager
                return null;
            }
            throw e;
        }
    }

}
