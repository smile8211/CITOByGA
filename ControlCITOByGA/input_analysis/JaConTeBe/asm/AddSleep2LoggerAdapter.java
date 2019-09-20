package asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.illinois.jacontebe.asm.AddSleepMethodAdapter;
import edu.illinois.jacontebe.asm.CodeTemplate;

public class AddSleep2LoggerAdapter extends AddSleepMethodAdapter implements
        Opcodes {
    private boolean insideMonitor;

    public AddSleep2LoggerAdapter(MethodVisitor mv, long sleepTime) {
        super(mv, sleepTime);
        insideMonitor = false;
    }

    @Override
    public void visitInsn(int opcode) {
        // Set the flag when codes enter and exit
        // synchronized block.
        if (opcode == MONITORENTER) {
            insideMonitor = true;
        }
        if (opcode == MONITOREXIT) {
            insideMonitor = false;
        }
        mv.visitInsn(opcode);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        mv.visitJumpInsn(opcode, label);
        // add sleep code after if null condition in
        // synchronized block.
        if (opcode == IFNULL && insideMonitor) {
            new CodeTemplate(mv).addSleepCodes(sTime);
        }
    }
}