package asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.illinois.jacontebe.asm.CodeTemplate;

/**
 * Insert global sequence driver code before and after statement: return
 * systemStub; This statement should be executed at the global sequence of 1.
 * 
 * @author Ziyi Lin
 *
 */
public class AddGS2RMILookUpMV extends MethodVisitor implements Opcodes {

    private CodeTemplate codeTemplate;

    public AddGS2RMILookUpMV(MethodVisitor mv) {
        super(ASM5, mv);
        codeTemplate = new CodeTemplate(mv);
    }

    public void visitJumpInsn(int opcode, Label label) {
        mv.visitJumpInsn(opcode, label);
        codeTemplate.addStartStep(ICONST_0);
    }

    /*
     * public void visitFieldInsn(int opcode, String owner, String name, String
     * desc){ mv.visitFieldInsn(opcode, owner, name, desc); if(opcode==GETFIELD
     * && name.equals("systemStub")){ codeTemplate.addEndStep(ICONST_0); } }
     */

}
