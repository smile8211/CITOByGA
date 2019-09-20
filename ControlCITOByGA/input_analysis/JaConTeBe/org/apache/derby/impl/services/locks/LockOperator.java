package org.apache.derby.impl.services.locks;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.services.locks.Lockable;

public class LockOperator {

    private SinglePool factory;
    private LockSpace space;
    private Object group;
    private Lockable ref;
    private Object qualifier;

    public LockOperator(SinglePool factory, Lockable ref, Object qualifier)
            throws StandardException {
        this.factory = factory;
        this.space = new LockSpace(null);
        this.ref = ref;
        this.qualifier = qualifier;

        Lock lock = new Lock(space, ref, qualifier);
        group = new Object();
        space.addLock(group, lock);
    }

    public void lock() throws StandardException {
        factory.lockObject(space, group, ref, qualifier, 0);
    }

    public void unlock() throws StandardException {

        factory.unlockGroup(space, group);
    }
}
