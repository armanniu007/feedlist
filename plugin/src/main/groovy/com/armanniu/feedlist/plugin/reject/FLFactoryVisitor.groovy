package com.armanniu.feedlist.plugin.reject

import com.armanniu.feedlist.plugin.signature.FLConstant
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class FLFactoryVisitor extends ClassVisitor {

    def debug = true

    FLFactoryVisitor(int api) {
        super(api)
    }

    FLFactoryVisitor(int api, ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        def mv = super.visitMethod(access, name, desc, signature, exceptions)
        if ("getAdapter" == name && "(Ljava/lang/String;)L${FLConstant.FLAdapter};" == desc) {
            return new FLFactoryMethodVisitor(Opcodes.ASM5, mv, access, name, desc)
        }
        return mv
    }


}