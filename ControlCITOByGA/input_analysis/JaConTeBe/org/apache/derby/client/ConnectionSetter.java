package org.apache.derby.client;

import org.apache.derby.client.am.LogicalConnection;

public class ConnectionSetter {

    public static void setLogicalConnection(ClientXAConnection40 connection,
            LogicalConnection logicalConnection) {
        connection.logicalConnection_ = logicalConnection;
    }
}
