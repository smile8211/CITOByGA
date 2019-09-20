package asm;

import org.objectweb.asm.MethodVisitor;

public class FutureTaskSleepFactory extends ActivationMVFactory {

    private long sleepTime;

    public FutureTaskSleepFactory(long st) {
        sleepTime = st;

    }

    @Override
    public MethodVisitor generateMethodVisitor(MethodVisitor mv, String name,
            String desc) {

        return new AddSleepMethod2FutureTaskAdapter(mv, sleepTime);
    }

}
