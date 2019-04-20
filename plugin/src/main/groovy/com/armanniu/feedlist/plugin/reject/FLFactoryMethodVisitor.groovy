package com.armanniu.feedlist.plugin.reject

import com.armanniu.feedlist.plugin.signature.FLConstant
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class FLFactoryMethodVisitor extends AdviceAdapter {

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api the ASM API version implemented by this visitor. Must be one
     *               of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param mv the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type Type}).
     */
    @SuppressWarnings("GroovyDocCheck")
    protected FLFactoryMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc)
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitTypeInsn(NEW, FLConstant.FACTORY_IMPL)
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, FLConstant.FACTORY_IMPL, "<init>", "()V", false)
        mv.visitFieldInsn(PUTFIELD, FLConstant.FACTORY_DEFAULT, "realFactory", "L${FLConstant.FACTORY};")
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)
    }
}
