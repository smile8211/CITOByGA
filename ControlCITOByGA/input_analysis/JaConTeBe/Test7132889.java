/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.illinois.jacontebe.framework.Reporter;

/**
 * Bug URL:https://bugs.openjdk.java.net/browse/JDK-7132889 
 * This is a race.
 * Reproduce environment: JDK 1.6.0, JDK 1.7.0 
 * This bug affects since JDK 1.6.0(included) and is fixed in 1.7.0_41
 * The expected exception is:
 * java.lang.RuntimeException: Key is valid
 * 

 * 
 * @collector Ziyi Lin
 */
public class Test7132889 {

    public static void main(String[] args) throws Exception {
        Reporter.reportStart("jdk7132889", 0, "race");
		Reporter.printWarning("1.6.0","1.7.0_41",null);
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Exception buggyException = null;
        try {
            Selector sel = Selector.open();
            int count = 100;
            while (count-- > 0) {
                final SocketChannel sc = SocketChannel.open();
                sc.configureBlocking(false);

                // close channel asynchronously
                Future<Void> result = pool.submit(new Callable<Void>() {
                    public Void call() throws IOException {
                        sc.close();
                        return null;
                    }
                });

                // attempt to register channel with Selector
                SelectionKey key = null;
                try {
                    key = sc.register(sel, SelectionKey.OP_READ);
                } catch (ClosedChannelException ignore) {
                }

                // ensure close is done
                result.get();

                // if we have a key then it should be invalid
                if (key != null && key.isValid()) {

                    buggyException = new RuntimeException("Key is valid");
                    break;
                }
            }
        } finally {
            pool.shutdown();
        }
        if (buggyException != null) {
            buggyException.printStackTrace();
        }
        Reporter.reportEnd(buggyException != null);
    }
}
