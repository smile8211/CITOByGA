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
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.OptionHelper;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://issues.apache.org/jira/browse/POOL-146
 * This is a wait-notify deadlock.
 * Reproduced environment: commons-pool 1.5, JDK 1.6.0_33
 * 
 * Options: --monitoroff, -mo: Turn monitor off. When monitor is turned on, it
 * reports the forever waiting message and stop the program
 * 
 * @collector Ziyi Lin
 */
public class Test146 {
    public static void main(String[] args) throws Exception {
        int timeOut = 30;
        Reporter.reportStart("pool146", timeOut, "wait-notify deadlock");
        if (!OptionHelper.optionParse(args)) {
            return;
        }
        Helpers.startWaitingMonitor(timeOut);
        Test146 test = new Test146();
        test.testBlockedKeyDoesNotBlockPoolImproved();
        Reporter.reportEnd(false);
    }

    public void testBlockedKeyDoesNotBlockPoolImproved() throws Exception {
        SimpleFactory factory = new SimpleFactory();
        GenericKeyedObjectPool pool = new GenericKeyedObjectPool(factory);
        pool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);
        pool.setMaxActive(1);
        pool.setMaxTotal(-1);

        // Borrow with one key
        Object obj = pool.borrowObject("one");

        // Borrow again with same key, should be blocked
        Runnable simple = new SimpleTestThread(pool, "one");
        Thread borrowThread = new Thread(simple);
        borrowThread.start();

        // Wait for sometime and borrow with another key
        // Should not block
        Thread.sleep(1000);
        pool.borrowObject("two");
        assert (pool.getNumActive("one") == 1);
        assert (pool.getNumActive("two") == 1);

        // Unblock and join the thread
        pool.returnObject("one", obj);
        borrowThread.join();
        assert (pool.getNumActive("one") == 0);
        assert (pool.getNumActive("two") == 1);
    }

    /*
     * Very simple test thread that just tries to borrow an object from the
     * provided pool with the specified key and returns it
     */
    static class SimpleTestThread implements Runnable {
        private final KeyedObjectPool _pool;
        private final String _key;

        public SimpleTestThread(KeyedObjectPool pool, String key) {
            _pool = pool;
            _key = key;
        }

        public void run() {
            try {
                Object obj = _pool.borrowObject(_key);
                _pool.returnObject(_key, obj);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    static class SimpleFactory implements KeyedPoolableObjectFactory {
        public SimpleFactory() {
            this(true);
        }

        public SimpleFactory(boolean valid) {
            this.valid = valid;
        }

        public Object makeObject(Object key) {
            synchronized (this) {
                activeCount++;
                if (activeCount > maxActive) {
                    throw new IllegalStateException(
                            "Too many active instances: " + activeCount);
                }
            }
            return String.valueOf(key) + String.valueOf(counter++);
        }

        public void destroyObject(Object key, Object obj) throws Exception {
            doWait(destroyLatency);
            synchronized (this) {
                activeCount--;
            }
            if (exceptionOnDestroy) {
                throw new Exception();
            }
        }

        public boolean validateObject(Object key, Object obj) {
            if (enableValidation) {
                return validateCounter++ % 2 == 0 ? evenValid : oddValid;
            } else {
                return valid;
            }
        }

        public void activateObject(Object key, Object obj) throws Exception {
            if (exceptionOnActivate) {
                if (!(validateCounter++ % 2 == 0 ? evenValid : oddValid)) {
                    throw new Exception();
                }
            }
        }

        public void passivateObject(Object key, Object obj) throws Exception {
            if (exceptionOnPassivate) {
                throw new Exception();
            }
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public void setDestroyLatency(long destroyLatency) {
            this.destroyLatency = destroyLatency;
        }

        public void setValidationEnabled(boolean b) {
            enableValidation = b;
        }

        void setEvenValid(boolean valid) {
            evenValid = valid;
        }

        void setValid(boolean valid) {
            evenValid = valid;
            oddValid = valid;
        }

        public void setThrowExceptionOnActivate(boolean b) {
            exceptionOnActivate = b;
        }

        public void setThrowExceptionOnDestroy(boolean b) {
            exceptionOnDestroy = b;
        }

        public void setThrowExceptionOnPassivate(boolean b) {
            exceptionOnPassivate = b;
        }

        int counter = 0;
        boolean valid;

        int activeCount = 0;
        int validateCounter = 0;
        boolean evenValid = true;
        boolean oddValid = true;
        boolean enableValidation = false;
        long destroyLatency = 0;
        int maxActive = Integer.MAX_VALUE;
        boolean exceptionOnPassivate = false;
        boolean exceptionOnActivate = false;
        boolean exceptionOnDestroy = false;

        private void doWait(long latency) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
}
