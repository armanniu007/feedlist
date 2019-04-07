package com.armanniu.feedlist.plugin.reject

import com.armanniu.feedlist.plugin.signature.FLConstant
import org.objectweb.asm.Label
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
        Label l0 = new Label()
        mv.visitLabel(l0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESTATIC, FLConstant.FACTORY, "getAdapter", "(Ljava/lang/String;)L${FLConstant.FLAdapter};", false)
        mv.visitVarInsn(ASTORE, 1)
        Label l1 = new Label()
        mv.visitLabel(l1)
        mv.visitVarInsn(ALOAD, 1)
        Label l2 = new Label()
        mv.visitJumpInsn(IFNULL, l2)
        Label l3 = new Label()
        mv.visitLabel(l3)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitInsn(ARETURN)
        mv.visitLabel(l2)
        mv.visitFrame(Opcodes.F_APPEND, 1, Arrays.asList(FLConstant.FLAdapter).toArray(), 0, null)
        mv.visitFieldInsn(GETSTATIC, FLConstant.FLAdapter, "DEFAULT", "L${FLConstant.FLAdapter};")
        mv.visitInsn(ARETURN)
        Label l4 = new Label()
        mv.visitLabel(l4)
        mv.visitLocalVariable("tplId", "Ljava/lang/String;", null, l0, l4, 0)
        mv.visitLocalVariable("adapter", "L${FLConstant.FLAdapter};", null, l1, l4, 1)
        mv.visitMaxs(1, 2)
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)
    }
}
