package asm;

import org.objectweb.asm.MethodVisitor;

import edu.illinois.jacontebe.asm.MvFactory;

public class NamingMVFactory implements MvFactory {

    @Override
    public MethodVisitor generateMethodVisitor(MethodVisitor mv, String name,
            String desc) {

        return new AddGS2NamingLookUpMV(mv);
    }

}
