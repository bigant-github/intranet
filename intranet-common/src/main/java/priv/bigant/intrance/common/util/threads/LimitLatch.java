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
package priv.bigant.intrance.common.util.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;


/**
 * Shared latch that allows the latch to be acquired a limited number of times after which all subsequent requests to
 * acquire the latch will be placed in a FIFO queue until one of the shares is returned.
 */
public class LimitLatch {

    private static final Logger log = LoggerFactory.getLogger(LimitLatch.class);


    private class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        @Override
        protected int tryAcquireShared(int ignored) {
            long newCount = count.incrementAndGet();
            if (!released && newCount > limit) {
                // Limit exceeded
                count.decrementAndGet();
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            count.decrementAndGet();
            return true;
        }
    }

    private Sync sync;
    private AtomicLong count;
    private volatile long limit;
    private volatile boolean released = false;

    /**
     * Returns the current count for the latch
     *
     * @return the current count for latch
     */
    public long getCount() {
        return count.get();
    }


    /**
     * Releases a shared latch, making it available for another thread to use.
     *
     * @return the previous counter value
     */
    public long countDown() {
        sync.releaseShared(0);
        long result = getCount();
        if (log.isDebugEnabled()) {
            log.debug("Counting down[" + Thread.currentThread().getName() + "] latch=" + result);
        }
        return result;
    }

}
