package asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.illinois.jacontebe.asm.CodeTemplate;

public class AddGS2NamingLookUpMV extends MethodVisitor implements Opcodes {

    private CodeTemplate codeTemplate;
    private boolean flag;

    public AddGS2NamingLookUpMV(MethodVisitor mv) {
        super(ASM5, mv);
        codeTemplate = new CodeTemplate(mv);
        flag = false;
    }

    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
        if (opcode == INVOKEINTERFACE && name.equals("lookup")) {
            flag = true;
        }
    }

    public void visitInsn(int opcode) {
        if (flag && opcode == ARETURN) {
            codeTemplate.addEndStep(ICONST_0);
        }
        mv.visitInsn(opcode);
    }

}
