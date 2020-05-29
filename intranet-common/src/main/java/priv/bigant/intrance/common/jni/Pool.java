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

package priv.bigant.intrance.common.jni;


/** Pool
 *
 * @author Mladen Turk
 */
public class Pool {

    /**
     * Create a new pool.
     * @param parent The parent pool.  If this is 0, the new pool is a root
     * pool.  If it is non-zero, the new pool will inherit all
     * of its parent pool's attributes, except the apr_pool_t will
     * be a sub-pool.
     * @return The pool we have just created.
    */
    public static native long create(long parent);

    /**
     * Clear all memory in the pool and run all the cleanups. This also destroys all
     * subpools.
     * @param pool The pool to clear
     * This does not actually free the memory, it just allows the pool
     *         to re-use this memory for the next allocation.
     */
    public static native void clear(long pool);

    /**
     * Destroy the pool. This takes similar action as apr_pool_clear() and then
     * frees all the memory.
     * This will actually free the memory
     * @param pool The pool to destroy
     */
    public static native void destroy(long pool);


    /*
     * Cleanup
     *
     * Cleanups are performed in the reverse order they were registered.  That is:
     * Last In, First Out.  A cleanup function can safely allocate memory from
     * the pool that is being cleaned up. It can also safely register additional
     * cleanups which will be run LIFO, directly after the current cleanup
     * terminates.  Cleanups have to take caution in calling functions that
     * create subpools. Subpools, created during cleanup will NOT automatically
     * be cleaned up.  In other words, cleanups are to clean up after themselves.
     */

    /*
     * User data management
     */

}
