package com.armanniu.feedlist.plugin.reject

import com.armanniu.feedlist.plugin.FLUtil
import com.armanniu.feedlist.plugin.signature.FLClassDesc
import com.armanniu.feedlist.plugin.signature.FLConstant
import com.armanniu.feedlist.plugin.signature.FLItem
import org.objectweb.asm.*

class FLFactoryWriter implements Opcodes {

    static byte[] generateClass(List<FLItem> itemList) {
        ClassWriter cw = new ClassWriter(0)
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, FLConstant.FACTORY_IMPL, null, "java/lang/Object", FLUtil.array(FLConstant.FACTORY))
        cw.visitSource("${FLConstant.FACTORY_IMPL}.java", null)
        cw.visitInnerClass(FLConstant.FACTORY, FLConstant.FLAdapter, FLConstant.FACTORY_NAME, ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE)
        visitField(cw)
        visitConstructor(itemList, cw)
        visitGetAdapterFromTplId(cw)
        visitGetAdapterFromId(cw)
        cw.visitEnd()
        return cw.toByteArray()
    }

    private static void visitGetAdapterFromId(ClassWriter cw) {
        MethodVisitor mv
        mv = cw.visitMethod(ACC_PUBLIC, "getAdapter", "(I)L${FLConstant.FLAdapter};", null, null)
        mv.visitCode()
        Label l0 = new Label()
        mv.visitLabel(l0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, FLConstant.FACTORY_IMPL, "cacheList", "Ljava/util/List;")
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true)
        mv.visitVarInsn(ASTORE, 2)
        Label l1 = new Label()
        mv.visitLabel(l1)
        mv.visitFrame(Opcodes.F_APPEND, 1, FLUtil.array("java/util/Iterator"), 0, null)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
        Label l2 = new Label()
        mv.visitJumpInsn(IFEQ, l2)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
        mv.visitTypeInsn(CHECKCAST, FLConstant.FLAdapter)
        mv.visitVarInsn(ASTORE, 3)
        Label l3 = new Label()
        mv.visitLabel(l3)
        mv.visitVarInsn(ALOAD, 3)
        mv.visitMethodInsn(INVOKEVIRTUAL, FLConstant.FLAdapter, "getId", "()I", false)
        mv.visitVarInsn(ILOAD, 1)
        Label l4 = new Label()
        mv.visitJumpInsn(IF_ICMPNE, l4)
        Label l5 = new Label()
        mv.visitLabel(l5)
        mv.visitVarInsn(ALOAD, 3)
        mv.visitInsn(ARETURN)
        mv.visitLabel(l4)
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        mv.visitJumpInsn(GOTO, l1)
        mv.visitLabel(l2)
        mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
        mv.visitInsn(ACONST_NULL)
        mv.visitInsn(ARETURN)
        Label l6 = new Label()
        mv.visitLabel(l6)
        mv.visitLocalVariable("adapter", "L${FLConstant.FLAdapter};", null, l3, l4, 3)
        mv.visitLocalVariable("this", "L${FLConstant.FACTORY_IMPL};", null, l0, l6, 0)
        mv.visitLocalVariable("id", "I", null, l0, l6, 1)
        mv.visitMaxs(2, 4)
        mv.visitEnd()
    }

    private static void visitGetAdapterFromTplId(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getAdapter", "(Ljava/lang/String;)L${FLConstant.FLAdapter};", null, null)
        mv.visitCode()
        Label l0 = new Label()
        mv.visitLabel(l0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, FLConstant.FACTORY_IMPL, "cacheList", "Ljava/util/List;")
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true)
        mv.visitVarInsn(ASTORE, 2)
        Label l1 = new Label()
        mv.visitLabel(l1)
        mv.visitFrame(Opcodes.F_APPEND, 1, FLUtil.array("java/util/Iterator"), 0, null)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
        Label l2 = new Label()
        mv.visitJumpInsn(IFEQ, l2)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
        mv.visitTypeInsn(CHECKCAST, FLConstant.FLAdapter)
        mv.visitVarInsn(ASTORE, 3)
        Label l3 = new Label()
        mv.visitLabel(l3)
        mv.visitVarInsn(ALOAD, 3)
        mv.visitMethodInsn(INVOKEVIRTUAL, FLConstant.FLAdapter, "getTplId", "()Ljava/lang/String;", false)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESTATIC, "android/text/TextUtils", "equals", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Z", false)
        Label l4 = new Label()
        mv.visitJumpInsn(IFEQ, l4)
        Label l5 = new Label()
        mv.visitLabel(l5)
        mv.visitVarInsn(ALOAD, 3)
        mv.visitInsn(ARETURN)
        mv.visitLabel(l4)
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        mv.visitJumpInsn(GOTO, l1)
        mv.visitLabel(l2)
        mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
        mv.visitInsn(ACONST_NULL)
        mv.visitInsn(ARETURN)
        Label l6 = new Label()
        mv.visitLabel(l6)
        mv.visitLocalVariable("adapter", "L${FLConstant.FLAdapter};", null, l3, l4, 3)
        mv.visitLocalVariable("this", "L${FLConstant.FACTORY_IMPL};", null, l0, l6, 0)
        mv.visitLocalVariable("tplId", "Ljava/lang/String;", null, l0, l6, 1)
        mv.visitMaxs(2, 4)
        mv.visitEnd()
    }

    private static void visitConstructor(List<FLItem> itemList, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitCode()
        Label l0 = new Label()
        mv.visitLabel(l0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        Label l1 = new Label()
        mv.visitLabel(l1)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitTypeInsn(NEW, "java/util/ArrayList")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
        mv.visitFieldInsn(PUTFIELD, FLConstant.FACTORY_IMPL, "cacheList", "Ljava/util/List;")
        itemList.forEach({
            Label l2 = new Label()
            mv.visitLabel(l2)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, FLConstant.FACTORY_IMPL, "cacheList", "Ljava/util/List;")
            mv.visitTypeInsn(NEW, FLConstant.FLAdapter)
            mv.visitInsn(DUP)
            mv.visitLdcInsn(it.tplId)
            mv.visitLdcInsn(getTypeFromItem(it))
            mv.visitInsn(ICONST_1)
            mv.visitMethodInsn(INVOKESPECIAL, FLConstant.FLAdapter, "<init>", "(Ljava/lang/String;Ljava/lang/reflect/Type;I)V", false)
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
            mv.visitInsn(POP)
        })
        Label l3 = new Label()
        mv.visitLabel(l3)
        mv.visitInsn(RETURN)
        Label l4 = new Label()
        mv.visitLabel(l4)
        mv.visitLocalVariable("this", "L${FLConstant.FACTORY_IMPL};", null, l0, l4, 0)
        mv.visitMaxs(6, 1)
        mv.visitEnd()
    }

    private static Type getTypeFromItem(FLItem item) {
        def sign = createSign(null)
        println("${item.flClass.className} --> $sign")
        return Type.getType(sign)
    }

    private static String createSign(FLClassDesc.Generic desc) {
        StringBuilder sb = new StringBuilder()
        createSignFromDesc(sb, desc)
        return sb.toString()
    }

    private static void createSignFromDesc(StringBuilder sb, FLClassDesc desc) {
        if (desc == null || desc.genericList == null || desc.genericList.size() == 0) {
            return
        }
        sb.append("<")
        desc.genericList.forEach({
            createSignFromDesc(sb, it)
        })
        sb.append(">;")
    }

    private static void createSignFromDesc(StringBuilder sb, FLClassDesc.Generic desc) {
        sb.append("L")
        sb.append(desc.className)
        if (desc != null && desc.classDesc != null) {
            createSignFromDesc(sb, desc.classDesc)
        }
        sb.append(";")
    }

    private static void visitField(ClassWriter cw) {
        def fv = cw.visitField(ACC_PRIVATE, "cacheList", "Ljava/util/List;", "Ljava/util/List<L${FLConstant.FLAdapter};>;", null)
        fv.visitEnd()
    }
}