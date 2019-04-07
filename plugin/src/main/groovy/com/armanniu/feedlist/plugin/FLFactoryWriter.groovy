package com.armanniu.feedlist.plugin

import com.armanniu.feedlist.plugin.signature.FLClassDesc
import com.armanniu.feedlist.plugin.signature.FLConstant
import com.armanniu.feedlist.plugin.signature.FLItem
import org.objectweb.asm.*

class FLFactoryWriter implements Opcodes {

    byte[] generateClass(List<FLItem> itemList) {
        def list = itemList.findAll {
            if (it.tplId == null || it.tplId.isEmpty()) {
                return false
            }
            if (!checkDescType(it.itemDesc)) {
                println("warning: item remove sign failure")
                println("---------------------------------\n")
                println(it.jsonObject.toString())
                println("\n---------------------------------")
                return false
            }
            return true
        }
        ClassWriter cw = new ClassWriter(0)
        MethodVisitor mv
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, FLConstant.FACTORY, null, 'java/lang/Object', null)
        cw.visitSource('FLFactory.java', null)

        def listSize = list == null ? 0 : list.size()
        for (int i = listSize; i > 0; i++) {
            cw.visitInnerClass(FLConstant.FACTORY + '$' + i, null, null, ACC_STATIC)
        }
        initFieldAdapterList()
        writeInit()
        writeCheck()
        initGetAdapter()
        mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)
        mv.visitCode()
        Label l0 = new Label()
        mv.visitLabel(l0)
        mv.visitLineNumber(12, l0)
        mv.visitTypeInsn(NEW, "java/util/ArrayList")
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
        mv.visitFieldInsn(PUTSTATIC, FLConstant.FACTORY, "sAdapterList", "Ljava/util/List;")
        def labelList = new ArrayList<Label>()
        def lineNumber = 30
        for (int i = 1; i <= listSize; i++) {
            Label l1 = new Label()
            mv.visitLabel(l1)
            mv.visitLineNumber(lineNumber += 1, l1)
            mv.visitTypeInsn(NEW, "${FLConstant.FACTORY}\$${i}")
            mv.visitInsn(DUP);
            mv.visitLdcInsn(list[i - 1].tplId)
            mv.visitMethodInsn(INVOKESPECIAL, "${FLConstant.FACTORY}\$${i}", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitVarInsn(ASTORE, 0)
            Label l2 = new Label()
            mv.visitLabel(l2)
            mv.visitLineNumber(lineNumber += 6, l2)
            mv.visitFieldInsn(GETSTATIC, FLConstant.FACTORY, "sAdapterList", "Ljava/util/List;")
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
            mv.visitInsn(POP)
            labelList.add(l2)
        }

        Label l7 = new Label()
        mv.visitLabel(l7)
        mv.visitLineNumber(lineNumber += 1, l7)
        mv.visitInsn(RETURN)
        for (int i = 1; i <= listSize; i++) {
            def desc = list[i - 1].itemDesc
            def sign = createSign(desc, sb)
            mv.visitLocalVariable("flAdapter\$${i}", "L${FLConstant.FLAdapter};", "L${FLConstant.FLAdapter}${sign}", labelList[i - 1], l7, i - 1)
        }
        mv.visitMaxs(listSize, listSize)
        mv.visitEnd()

        return cw.toByteArray()
    }

    private static String createSign(FLClassDesc.Desc desc) {
        StringBuilder sb = new StringBuilder()
        sb.append("<")
        createFromDesc(desc, sb)
        sb.append(">")
        sb.append(";")
    }

    private static void createFromDesc(FLClassDesc.Desc desc, StringBuilder sb) {
        if (desc == null) {
            return
        }
        sb.append(desc.type)
        sb.append(desc.className)
        createSignFromClassDesc(desc.classDesc, sb)
    }

    private static void createSignFromClassDesc(FLClassDesc desc, StringBuilder sb) {
        if (desc == null || desc.descList == null || desc.descList.isEmpty()) {
            sb.append(";")
            return
        }
        sb.append("<")
        desc.descList.forEach({
            createFromDesc(it, sb)
            sb.append(";")
        })
        sb.append(">")
        sb.append(";")
    }

    private void initGetAdapter() {
        MethodVisitor mv
        mv = cw.visitMethod(ACC_PUBLIC, 'getAdapter', "(Ljava/lang/String;)L${FLConstant.FLAdapter};", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(22, l0);
        mv.visitFieldInsn(GETSTATIC, FLConstant.FACTORY, 'sAdapterList', 'Ljava/util/List;');
        mv.visitMethodInsn(INVOKEINTERFACE, 'java/util/List', 'iterator', '()Ljava/util/Iterator;', true);
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitFrame(Opcodes.F_APPEND, 1, Arrays.asList("java/util/Iterator").toArray(), 0, null);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, 'java/util/Iterator', 'hasNext', '()Z', true);
        Label l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEINTERFACE, 'java/util/Iterator', 'next', '()Ljava/lang/Object;', true);
        mv.visitTypeInsn(CHECKCAST, FLConstant.FLAdapter);
        mv.visitVarInsn(ASTORE, 3);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(23, l3);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, FLConstant.FLAdapter, 'getTplId', '()Ljava/lang/String;', false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, FLConstant.FACTORY, 'check', '(Ljava/lang/String;Ljava/lang/String;)Z', false);
        Label l4 = new Label();
        mv.visitJumpInsn(IFEQ, l4);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLineNumber(24, l5);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l4);
        mv.visitLineNumber(26, l4);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l2);
        mv.visitLineNumber(27, l2);
        mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        mv.visitFieldInsn(GETSTATIC, FLConstant.FLAdapter, 'DEFAULT', "L${FLConstant.FLAdapter};");
        mv.visitInsn(ARETURN);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitLocalVariable('adapter', "L${FLConstant.FLAdapter};", null, l3, l4, 3);
        mv.visitLocalVariable('this', "L${FLConstant.FACTORY};", null, l0, l6, 0);
        mv.visitLocalVariable('tplId', 'Ljava/lang/String;', null, l0, l6, 1);
        mv.visitMaxs(2, 4);
        mv.visitEnd();
    }

    private void writeCheck() {
        MethodVisitor mv
        mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, 'check', '(Ljava/lang/String;Ljava/lang/String;)Z', null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(15, l0);
        mv.visitVarInsn(ALOAD, 0);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNULL, l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, 'java/lang/String', 'length', '()I', false);
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitJumpInsn(IFNULL, l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, 'java/lang/String', 'length', '()I', false);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNE, l2);
        mv.visitLabel(l1);
        mv.visitLineNumber(16, l1);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l2);
        mv.visitLineNumber(18, l2);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, 'java/lang/String', 'equals', '(Ljava/lang/Object;)Z', false);
        mv.visitInsn(IRETURN);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable('src', 'Ljava/lang/String;', null, l0, l3, 0);
        mv.visitLocalVariable('desc', 'Ljava/lang/String;', null, l0, l3, 1);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void writeInit() {
        MethodVisitor mv
        mv = cw.visitMethod(ACC_PUBLIC, '<init>', '()V', null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(10, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, 'java/lang/Object', '<init>', '()V', false);
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable('this', "L${FLConstant.FACTORY};", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private boolean checkDescType(FLClassDesc.Desc desc) {
        if (desc == null) {
            return false
        }
        if (desc.type != FLConstant.TYPE_CLASS) {
            return false
        }
        if (desc.classDesc != null && desc.classDesc.descList != null) {
            def list = desc.classDesc.descList
            for (int i = 0; i < list.size(); i++) {
                def childDesc = list[i]
                if (childDesc != null && !checkDescType(childDesc)) {
                    return false
                }
            }
        }
        return true
    }

    private void initFieldAdapterList() {
        FieldVisitor fv
        fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, 'sAdapterList', 'Ljava/util/List;', "Ljava/util/List<L${FLConstant.FLAdapter};>;", null);
        fv.visitEnd()
    }
}