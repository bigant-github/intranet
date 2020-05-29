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

package priv.bigant.intrance.common.util.net.jsse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.net.SSLSessionManager;
import priv.bigant.intrance.common.util.net.SSLSupport;
import priv.bigant.intrance.common.util.net.openssl.ciphers.Cipher;
import priv.bigant.intrance.common.util.res.StringManager;
import sun.rmi.runtime.Log;

import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * JSSESupport
 * <p>
 * Concrete implementation class for JSSE Support classes.
 * <p>
 * This will only work with JDK 1.2 and up since it depends on JDK 1.2's certificate support
 *
 * @author EKR
 * @author Craig R. McClanahan Parts cribbed from JSSECertCompat Parts cribbed from CertificatesValve
 */
public class JSSESupport implements SSLSupport, SSLSessionManager {

    private static final Logger log = LoggerFactory.getLogger(JSSESupport.class);

    private static final StringManager sm = StringManager.getManager(JSSESupport.class);

    private static final Map<String, Integer> keySizeCache = new HashMap<>();

    static {
        for (Cipher cipher : Cipher.values()) {
            for (String jsseName : cipher.getJsseNames()) {
                keySizeCache.put(jsseName, Integer.valueOf(cipher.getStrength_bits()));
            }
        }
    }


    private SSLSession session;


    public JSSESupport(SSLSession session) {
        this.session = session;
    }

    @Override
    public String getCipherSuite() throws IOException {
        // Look up the current SSLSession
        if (session == null)
            return null;
        return session.getCipherSuite();
    }

    @Override
    public java.security.cert.X509Certificate[] getPeerCertificateChain() throws IOException {
        // Look up the current SSLSession
        if (session == null)
            return null;

        Certificate[] certs = null;
        try {
            certs = session.getPeerCertificates();
        } catch (Throwable t) {
            log.debug(sm.getString("jsseSupport.clientCertError"), t);
            return null;
        }
        if (certs == null) return null;

        java.security.cert.X509Certificate[] x509Certs =
                new java.security.cert.X509Certificate[certs.length];
        for (int i = 0; i < certs.length; i++) {
            if (certs[i] instanceof java.security.cert.X509Certificate) {
                // always currently true with the JSSE 1.1.x
                x509Certs[i] = (java.security.cert.X509Certificate) certs[i];
            } else {
                try {
                    byte[] buffer = certs[i].getEncoded();
                    CertificateFactory cf =
                            CertificateFactory.getInstance("X.509");
                    ByteArrayInputStream stream =
                            new ByteArrayInputStream(buffer);
                    x509Certs[i] = (java.security.cert.X509Certificate)
                            cf.generateCertificate(stream);
                } catch (Exception ex) {
                    log.info(sm.getString(
                            "jseeSupport.certTranslationError", certs[i]), ex);
                    return null;
                }
            }
            if (log.isTraceEnabled())
                log.trace("Cert #" + i + " = " + x509Certs[i]);
        }
        if (x509Certs.length < 1)
            return null;
        return x509Certs;
    }


    /**
     * {@inheritDoc}
     * <p>
     * This returns the effective bits for the current cipher suite.
     */
    @Override
    public Integer getKeySize() throws IOException {
        // Look up the current SSLSession
        if (session == null) {
            return null;
        }

        return keySizeCache.get(session.getCipherSuite());
    }

    @Override
    public String getSessionId()
            throws IOException {
        // Look up the current SSLSession
        if (session == null)
            return null;
        // Expose ssl_session (getId)
        byte[] ssl_session = session.getId();
        if (ssl_session == null)
            return null;
        StringBuilder buf = new StringBuilder();
        for (int x = 0; x < ssl_session.length; x++) {
            String digit = Integer.toHexString(ssl_session[x]);
            if (digit.length() < 2) buf.append('0');
            if (digit.length() > 2) digit = digit.substring(digit.length() - 2);
            buf.append(digit);
        }
        return buf.toString();
    }


    public void setSession(SSLSession session) {
        this.session = session;
    }


    /**
     * Invalidate the session this support object is associated with.
     */
    @Override
    public void invalidateSession() {
        session.invalidate();
    }

    @Override
    public String getProtocol() throws IOException {
        if (session == null) {
            return null;
        }
        return session.getProtocol();
    }
}

