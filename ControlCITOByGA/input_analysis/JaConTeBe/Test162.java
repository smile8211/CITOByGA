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
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://issues.apache.org/jira/browse/POOL-162 
 * This is a wait-notify deadlock.
 * Reproduce environment: commons-pool 1.5, JDK 1.6.0_33.
 * 
 *  A thread supposed to notify a later started
 * thread is interrupted and terminates, so no one will notify the other thread
 * from waiting.
 * 
 * @collector Ziyi Lin
 *
 */
public class Test162 {
    public class SimpleFactory implements PoolableObjectFactory {

        public SimpleFactory() {
            this(true);
        }

        public SimpleFactory(boolean valid) {

            this(valid, valid);
        }

        public SimpleFactory(boolean evalid, boolean ovalid) {
            evenValid = evalid;
            oddValid = ovalid;
        }

        void setValid(boolean valid) {
            setEvenValid(valid);
            setOddValid(valid);
        }

        void setEvenValid(boolean valid) {
            evenValid = valid;

        }

        void setOddValid(boolean valid) {
            oddValid = valid;
        }

        public void setThrowExceptionOnPassivate(boolean bool) {

            exceptionOnPassivate = bool;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public void setDestroyLatency(long destroyLatency) {
            this.destroyLatency = destroyLatency;
        }

        public void setMakeLatency(long makeLatency) {
            this.makeLatency = makeLatency;
        }

        public Object makeObject() {
            synchronized (this) {
                activeCount++;
                if (activeCount > maxActive) {
                    throw new IllegalStateException(
                            "Too many active instances: " + activeCount);
                }
            }
            if (makeLatency > 0) {
                doWait(makeLatency);
            }
            return String.valueOf(makeCounter++);
        }

        public void destroyObject(Object obj) throws Exception {
            if (destroyLatency > 0) {
                doWait(destroyLatency);
            }
            synchronized (this) {
                activeCount--;
            }
            if (exceptionOnDestroy) {
                throw new Exception();
            }
        }

        public boolean validateObject(Object obj) {
            if (enableValidation) {
                return validateCounter++ % 2 == 0 ? evenValid : oddValid;
            } else {
                return true;
            }
        }

        public void activateObject(Object obj) throws Exception {
            if (exceptionOnActivate) {
                if (!(validateCounter++ % 2 == 0 ? evenValid : oddValid)) {
                    throw new Exception();
                }
            }
        }

        public void passivateObject(Object obj) throws Exception {
            if (exceptionOnPassivate) {
                throw new Exception();
            }
        }

        int makeCounter = 0;
        int validateCounter = 0;

        int activeCount = 0;

        boolean evenValid = true;
        boolean oddValid = true;
        boolean exceptionOnPassivate = false;

        boolean exceptionOnActivate = false;

        boolean exceptionOnDestroy = false;

        boolean enableValidation = true;

        long destroyLatency = 0;

        long makeLatency = 0;

        int maxActive = Integer.MAX_VALUE;

        public boolean isThrowExceptionOnActivate() {
            return exceptionOnActivate;
        }

        public void setThrowExceptionOnActivate(boolean b) {
            exceptionOnActivate = b;
        }

        public void setThrowExceptionOnDestroy(boolean b) {
            exceptionOnDestroy = b;
        }

        public boolean isValidationEnabled() {
            return enableValidation;
        }

        public void setValidationEnabled(boolean b) {
            enableValidation = b;
        }

        private void doWait(long latency) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException ex) {
                // ignore
            }
        }

    }

    public void testWhenExhaustedBlockInterupt() throws Exception {
        GenericObjectPool pool = new GenericObjectPool(new SimpleFactory());
        pool.setMaxActive(1);
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);

        Object obj1 = pool.borrowObject();

        // Make sure on object was obtained
        if (obj1 == null) {
            throw new Exception("obj1 is null");
        }

        // Create a separate thread to try and borrow another object
        WaitingTestThread wtt = new WaitingTestThread(pool, 200);
        wtt.start();
        // Give wtt time to start
        Thread.sleep(200);
        wtt.interrupt();
        // Give interupt time to take effect
        Thread.sleep(200);
        // Check thread was interrupted
        if (!(wtt._thrown instanceof InterruptedException)) {
            throw new Exception(
                    "wtt._thrown is not instance of InterruptedException.");
        }

        // Return object to the pool
        pool.returnObject(obj1);
        // Bug POOL-162 - check there is now an object in the pool
        // pool.setMaxWait(10L);
        Object obj2 = null;
        obj2 = pool.borrowObject();
        if (obj2 == null) {
            throw new Exception("obj2 is null");
        }
        pool.returnObject(obj2);
        pool.close();
    }

    /*
     * Very simple test thread that just tries to borrow an object from the
     * provided pool returns it after a wait
     */
    static class WaitingTestThread extends Thread {
        private final GenericObjectPool _pool;
        private final long _pause;
        private Throwable _thrown;

        public WaitingTestThread(GenericObjectPool pool, long pause) {
            _pool = pool;
            _pause = pause;
            _thrown = null;
        }

        public void run() {
            try {
                Object obj = _pool.borrowObject();
                Thread.sleep(_pause);
                _pool.returnObject(obj);
            } catch (Exception e) {
                _thrown = e;
            }
        }
    }

    public static void main(String args[]) throws Exception {
        int timeOut = 30;
        Reporter.reportStart("pool162", timeOut, "wait-notify deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startWaitingMonitor(timeOut);
        Test162 t = new Test162();
        t.testWhenExhaustedBlockInterupt();
        Reporter.reportEnd(false);
    }
}