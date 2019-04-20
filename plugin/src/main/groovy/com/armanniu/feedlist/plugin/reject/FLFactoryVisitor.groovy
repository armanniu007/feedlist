package com.armanniu.feedlist.plugin.reject


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class FLFactoryVisitor extends ClassVisitor {

    FLFactoryVisitor(int api) {
        super(api)
    }

    FLFactoryVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        def mv = super.visitMethod(access, name, desc, signature, exceptions)
        if ("<init>" == name) {
            return new FLFactoryMethodVisitor(Opcodes.ASM5, mv, access, name, desc)
        }
        return mv
    }


}