package com.armanniu.feedlist.plugin

import com.android.build.api.transform.JarInput
import com.armanniu.feedlist.plugin.signature.FLClass
import com.armanniu.feedlist.plugin.signature.FLClassDesc
import com.armanniu.feedlist.plugin.signature.FLConstant
import com.armanniu.feedlist.plugin.signature.FLItem
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.json.simple.JSONObject
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile

class FLClassParser implements FLJson {
    private static debug = true
    private def itemList = new ArrayList<FLItem>()
    private def classList = new ArrayList<FLClass>()
    private def classDescList = new ArrayList<FLClassDesc>()
    private def ignoreList = new ArrayList<String>(Arrays.asList(FLConstant.OBJECT))
    private def ignorePackageList = new ArrayList<String>(Arrays.asList(
            "android/",
            "androidx/",
            "java/",
            "kotlin/",
            "org/intellij/lang",
            "org/jetbrains/annotations"))

    FLClassParser(Project project) {

    }

    void fromDirector(File dir) {
        if (!dir.isDirectory()) {
            if (debug) {
                println("$TAG $dir is not directory")
            }
            return
        }
        dir.eachFileRecurse {
            def filePath = it.absolutePath
            if (filePath.endsWith(".class")
                    && !filePath.contains('R$')
                    && !filePath.contains('R.class')
                    && !filePath.contains("BuildConfig.class")) {
                ClassReader classReader = new ClassReader(it.bytes)
                ClassVisitor cv = new FLClassReader(Opcodes.ASM5, it.absolutePath)
                classReader.accept(cv, 0)
            }
        }
    }

    void fromJar(JarInput jarInput) {
        if (!jarInput.file.getAbsolutePath().endsWith(".jar")) {
            return
        }
        def jarFile = new JarFile(jarInput.file)
        def enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            def jarEntry = (JarEntry) enumeration.nextElement()
            def entryName = jarEntry.getName()
            if (entryName.endsWith(".class")
                    && !entryName.contains('R$')
                    && !entryName.contains('R.class')
                    && !entryName.contains("BuildConfig.class")) {
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                ClassVisitor cv = new FLClassReader(Opcodes.ASM5, jarInput.file.absolutePath)
                classReader.accept(cv, 0)
                inputStream.close()
            }
        }
        jarFile.close()
    }

    def getItemList() {
        return itemList
    }

    void parseFlItems() {
        new ArrayList<>(itemList).forEach({
            parseFlItem(it)
        })
    }

    private void parseFlItem(FLItem flItem) {
        if (flItem.itemDesc != null) {
            return
        }
        if (debug) {
            println("parseFlItem-->${flItem.toString()}")
        }
        parseFlItemClass(flItem.flClass)
    }

    private FLClassDesc.Desc parseFlItemClass(FLClass flClass) {
        if (flClass == null || ignoreList.contains(flClass.className)) { //如果是忽略的类，停止查找
            return null
        }
        if (debug) {
            println("parseFlItemClass-->${flClass.toString()}")
        }
        def item = itemList.find({ it.flClass.className == flClass.className })
        if (item != null && item.itemDesc != null) { //从记录里边查找
            return item.itemDesc
        }
        def classDesc = classDescList.find({ it.flClass.className == flClass.className })
        if (classDesc == null) { //创建新的desc
            classDesc = FLClassDesc.create(flClass)
            classDescList.add(classDesc)
        }
        if (flClass.className == FLConstant.FEED_ITEM) {
            if (classDesc.descList == null || classDesc.descList.size() < 1) {
                throw new IllegalAccessException("${FLConstant.FEED_ITEM} error")
            }
            if (classDesc.descList == null || classDesc.descList.isEmpty()) {
                return null
            }
            def first = classDesc.descList.first()
            return first
        }

        for (int i = 0; i < classDesc.superClassDescList.size(); i++) {
            def it = classDesc.superClassDescList[i]
            if (debug) {
                println("forEach ${it.toString()}")
            }
            def superFlClass = classList.find({ that -> that.className == it.flClass.className })
            def superDesc = parseFlItemClass(superFlClass)
            if (debug) {
                println("forEach2 ${superDesc}")
            }
            if (superDesc != null) { //merge
                def desc
                if (it.descList == null || it.descList.size() <= superDesc.index) {
                    desc = new FLClassDesc.Desc(superDesc.classDesc, superDesc.type, superDesc.className, superDesc.isInterface, superDesc.descName, Integer.MAX_VALUE)
                } else {
                    def itDesc = it.descList[superDesc.index]
                    if (itDesc.type == FLConstant.TYPE_CLASS) {
                        desc = itDesc
                    } else {
                        desc = classDesc.descList.find({ that -> that.descName == itDesc.className })
                    }
                }
                if (debug) {
                    println("forEach3 ${desc}")
                }
                if (desc != null) {
                    def record = itemList.find({ it.flClass.className == flClass.className })
                    if (record == null) {
                        record = new FLItem(null, flClass)
                        record.itemDesc = desc
                        itemList.add(record)
                    } else {
                        record.itemDesc = desc
                    }
                    return desc
                }
            }
        }
        ignoreList.add(flClass.className)
        return null

    }

    @Override
    JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("itemList", FLUtil.fromArray(itemList))
        jsonObject.put("classList", FLUtil.fromArray(classList))
        jsonObject.put("classDescList", FLUtil.fromArray(classDescList))
        jsonObject.put("ignoreList", FLUtil.fromArray(ignoreList))
        return jsonObject
    }

    @Override
    public String toString() {
        return getJsonObject().toString();
    }

    private class FLClassReader extends ClassVisitor implements Opcodes {

        private static boolean debug = true
        private def isFeedItem = false
        private def version = 0
        private def access = 0
        private def name = ""
        private def signature = ""
        private def superName = ""
        private def tplId = ""
        private def String[] interfaces
        private def classPath = ""
        private def ignore = false

        FLClassReader(int i, String classPath) {
            super(i)
        }

        FLClassReader(int i, ClassVisitor classVisitor, String classPath) {
            super(i, classVisitor)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            this.version = version
            this.access = access
            this.name = name
            this.signature = signature
            this.superName = superName
            this.interfaces = interfaces
            this.ignore = ignorePackageList.find({ name.startsWith(it) }) != null
            if (debug && !ignore) {
                println("FLClassReader ---> visit:: version=$version, access=$access, name=$name, signature=$signature, superName=$superName, interfaces=$interfaces")
            }
        }

        @Override
        void visitEnd() {
            super.visitEnd()
            if (ignore) {
                return
            }
            def flClass = new FLClass(name, signature, superName, interfaces, "")
            classList.add(flClass)
            if (isFeedItem) {
                itemList.add(new FLItem(tplId, flClass))
            }
            if (debug && !ignore) {
                println("FLClassReader ---> visitEnd")
            }
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (debug && !ignore) {
                println("FLClassReader ---> visitAnnotation:: desc =$desc visible = $visible")
            }
            isFeedItem = FLConstant.ANNOTATION_FEED_ITEM == desc
            if (isFeedItem) {
                return new MyAnnotationVisitor(ASM5, super.visitAnnotation(desc, visible))
            } else {
                return super.visitAnnotation(desc, visible)
            }
        }

        @Override
        AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            if (debug && !ignore) {
                println("FLClassReader ---> visitTypeAnnotation:: typeRef =$typeRef desc = $desc visible = $visible")
            }
            return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return super.visitMethod(access, name, desc, signature, exceptions)
        }

        class MyAnnotationVisitor extends AnnotationVisitor {

            MyAnnotationVisitor(int api) {
                super(api)
            }

            MyAnnotationVisitor(int api, AnnotationVisitor av) {
                super(api, av)
            }

            @Override
            void visit(String name, Object value) {
                super.visit(name, value)
                if (debug && !ignore) {
                    println("MyAnnotationVisitor ---> visit:: name = $name value = $value")
                }
                if (name == "value") {
                    tplId = value.toString()
                }
            }

            @Override
            void visitEnd() {
                super.visitEnd()
                if (debug && !ignore) {
                    println("MyAnnotationVisitor ---> visitEnd")
                }
            }

            @Override
            void visitEnum(String name, String desc, String value) {
                super.visitEnum(name, desc, value)
                if (debug && !ignore) {
                    println("MyAnnotationVisitor ---> visitEnum:: name = $name desc = $desc value = $value")
                }
            }

            @Override
            AnnotationVisitor visitAnnotation(String name, String desc) {
                if (debug && !ignore) {
                    println("MyAnnotationVisitor ---> visitAnnotation:: name = $name desc = $desc")
                }
                return super.visitAnnotation(name, desc)
            }
        }
    }
}