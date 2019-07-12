/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package priv.bigant.intrance.common.coyote.http11.servlet;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides an input stream for reading binary data from a client request,
 * including an efficient <code>readLine</code> method for reading data one line
 * at a time. With some protocols, such as HTTP POST and PUT, a
 * <code>ServletInputStream</code> object can be used to read data sent from the
 * client.
 * <p>
 * A <code>ServletInputStream</code> object is normally retrieved via the
 * <p>
 * This is an abstract class that a servlet container implements. Subclasses of
 * this class must implement the <code>java.io.InputStream.read()</code> method.
 *
 */
public abstract class ServletInputStream extends InputStream {

    /**
     * Does nothing, because this is an abstract class.
     */
    protected ServletInputStream() {
        // NOOP
    }

    /**
     * Has the end of this InputStream been reached?
     *
     * @return <code>true</code> if all the data has been read from the stream,
     * else <code>false</code>
     *
     * @since Servlet 3.1
     */
    public abstract boolean isFinished();

    /**
     * Can data be read from this InputStream without blocking?
     * Returns  If this method is called and returns false, the container will
     *
     * @return <code>true</code> if data can be read without blocking, else
     * <code>false</code>
     *
     * @since Servlet 3.1
     */
    public abstract boolean isReady();

}
