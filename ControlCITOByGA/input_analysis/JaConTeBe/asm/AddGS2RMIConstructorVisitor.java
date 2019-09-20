package asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.illinois.jacontebe.asm.CodeTemplate;

/**
 * Insert global sequence driver code before and after statement:
 * this.systemStub = systemStub; This statement should be executed at the global
 * sequence of 2.
 *
 * @author Ziyi Lin
 *
 */
public class AddGS2RMIConstructorVisitor extends MethodVisitor implements
        Opcodes {

    private CodeTemplate codeTemplate;

    public AddGS2RMIConstructorVisitor(MethodVisitor mv) {
        super(ASM5, mv);
        codeTemplate = new CodeTemplate(mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
        if (opcode == INVOKESPECIAL) {
            codeTemplate.addStartStep(ICONST_1);
        }
    }

    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            codeTemplate.addEndOfSequence();
        }
        mv.visitInsn(opcode);
    }
}
