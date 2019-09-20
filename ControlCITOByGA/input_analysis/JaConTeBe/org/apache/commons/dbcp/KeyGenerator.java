package org.apache.commons.dbcp;

import static org.mockito.Mockito.mock;

import org.apache.commons.dbcp.PoolingConnection.PStmtKey;

public class KeyGenerator {

    public static PStmtKey generateKey() {
        PStmtKey key = mock(PStmtKey.class);
        return key;
    }
}
