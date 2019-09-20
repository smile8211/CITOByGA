package asm;

import org.objectweb.asm.MethodVisitor;

import edu.illinois.jacontebe.asm.AddSleepMethodAdapter;
import edu.illinois.jacontebe.asm.CodeTemplate;

public class AddSleepMethod2FutureTaskAdapter extends AddSleepMethodAdapter {

    private boolean flag;

    public AddSleepMethod2FutureTaskAdapter(MethodVisitor mv, long sleepTime) {
        super(mv, sleepTime);
        flag = false;
    }

    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
        if (opcode == INVOKEVIRTUAL && name.equals("compareAndSetState")) {
            flag = true;
        }
    }

    public void visitVarInsn(int opcode, int var) {
        if (flag) {
            new CodeTemplate(mv).addSleepCodes(sTime);
            flag = false;
        }
        mv.visitVarInsn(opcode, var);
    }

}
