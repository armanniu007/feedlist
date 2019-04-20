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
    private def originalItemList = new ArrayList<FLItem>()
    private def recordItemList = new ArrayList<FLItem>()
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
        return originalItemList
    }

    void parseFlItems() {
        recordItemList.removeAll(originalItemList)
        recordItemList.addAll(originalItemList)
        originalItemList.forEach({
            parseFlItem(it)
        })
    }

    private void parseFlItem(FLItem flItem) {
        if (flItem.labelItemGenericList != null) {
            return
        }
        if (debug) {
            println("")
            println("-------------| parseFlItem sta |-----------------")
            println("--> parseFlItem ${flItem}")
        }
        def labelItem = parseFlItemClass(flItem.flClass)
        if (debug) {
            println("<-- parseFlItem ${labelItem}")
            println("-------------| parseFlItem end |-----------------")
            println("")
        }
    }

    private List<FLClassDesc.Generic> parseFlItemClass(FLClass flClass) {
        if (debug) {
            println("")
            println("--> parseFlItemClass")
            println("flClass: ${flClass}")
        }
        if (flClass == null) { //如果是忽略的类，停止查找
            return null
        }
        if (ignoreList.contains(flClass.className)) {
            if (debug) {
                println("<-- parseFlItemClass ignore")
                println("")
            }
            return null
        }
        def item = recordItemList.find({ it.flClass.className == flClass.className })
        if (item != null && item.labelItemGenericList != null) { //从记录里边查找
            if (debug) {
                println("<-- parseFlItemClass from record")
                println("")
            }
            return item.labelItemGenericList
        }
        def rootLabels = getRootItemGenericList(flClass)
        if (rootLabels != null) {
            if (debug) {
                println("<-- parseFlItemClass from root")
                println("")
            }
            recordLabelItemGeneric(flClass, rootLabels)
            return rootLabels
        }
        def classDesc = getClassDesc(flClass)
        def labelGenericList = classDesc.labelGenericList
        for (int i = 0; i < labelGenericList.size(); i++) {
            def labelGenericClassDesc = labelGenericList[i]
            if (debug) {
                println("labelGenericClassDesc: ${labelGenericClassDesc}")
            }
            if (labelGenericClassDesc == null) {
                continue
            }
            def realClass = classList.find({ that -> that.className == labelGenericClassDesc.flClass.className })
            if (realClass == null) {
                continue
            }
            def realClassDesc = getClassDesc(realClass)
            def realClassFlItemLabels = parseFlItemClass(realClass)
            if (debug) {
                println("realClassFlItemLabels: ${realClassFlItemLabels}")
            }
            if (realClassFlItemLabels == null) {
                continue
            }
            if (labelGenericClassDesc.genericList == null || labelGenericClassDesc.genericList.isEmpty()) {
                def clone = cloneGenericList(realClassFlItemLabels)
                setGenericIndexInvalid(clone)
                recordLabelItemGeneric(flClass, clone)
                if (debug) {
                    println("clone: ${clone}")
                    println("<-- parseFlItemClass no label")
                }
                return clone
            }
            //merge generics

            //label index
            def labeledGenericsClone = cloneGenericList(labelGenericClassDesc.genericList)
            setGenericIndexInvalid(labeledGenericsClone)
            mergeGenericLabelsFromRight(labeledGenericsClone, classDesc.genericList)
            if (debug) {
                println("labeledGenericsClone: ${labeledGenericsClone}")
            }
            def mergedFlItemLabels = mergeFlItemLabels(realClassFlItemLabels, realClassDesc.genericList, labeledGenericsClone)
            if (debug) {
                if (debug) {
                    println("mergedFlItemLabels: ${mergedFlItemLabels}")
                    println("<-- parseFlItemClass merge")
                }
            }
            recordLabelItemGeneric(flClass, mergedFlItemLabels)
            return mergedFlItemLabels
        }
        ignoreList.add(flClass.className)
        if (debug) {
            println("<-- parseFlItemClass not found")
            println("")
        }
        return null

    }

    private void recordLabelItemGeneric(FLClass flClass, List<FLClassDesc.Generic> labelItemGenericList) {
        def record = recordItemList.find({ it.flClass.className == flClass.className })
        if (record == null) {
            record = new FLItem(null, flClass)
            record.labelItemGenericList = labelItemGenericList
            recordItemList.add(record)
        } else {
            record.labelItemGenericList = labelItemGenericList
        }
    }

    private List<FLClassDesc.Generic> getRootItemGenericList(FLClass flClass) {
        FLClassDesc classDesc = getClassDesc(flClass)
        if (flClass.className == FLConstant.FEED_ITEM) {
            def genericList = classDesc.genericList
            return cloneGenericList(genericList)
        }
        return null
    }

    private FLClassDesc getClassDesc(FLClass flClass) {
        def classDesc = classDescList.find({ it.flClass.className == flClass.className })
        if (classDesc == null) { //创建新的desc
            classDesc = FLClassDesc.create(flClass)
            classDescList.add(classDesc)
        }
        classDesc
    }

    private static List<FLClassDesc.Generic> cloneGenericList(List<FLClassDesc.Generic> genericList) {
        def list = new ArrayList<FLClassDesc.Generic>()
        if (genericList == null) {
            return list
        } else {
            def size = genericList.size()
            for (int i = 0; i < size; i++) {
                list.add(genericList[i].clone())
            }
            return list
        }
    }

    private static void setGenericIndexInvalid(List<FLClassDesc.Generic> genericList) {
        if (genericList == null || genericList.isEmpty()) {
            return
        }
        genericList.forEach({
            it.index = Integer.MAX_VALUE
            if (it.classDesc != null) {
                setGenericIndexInvalid(it.classDesc.genericList)
            }
        })
    }

    private static void mergeGenericLabelsFromRight(List<FLClassDesc.Generic> left, List<FLClassDesc.Generic> right) {
        if (left == null || left.isEmpty() || right == null || right.isEmpty()) {
            return
        }
        left.forEach({ lg ->
            def lrLabel = right.find({ rg ->
                if (lg.labelName == null || lg.labelName.length() == 0) {
                    rg.labelName == lg.className
                } else {
                    rg.labelName == lg.labelName
                }
            })
            if (lrLabel != null) {
                lg.className = lrLabel.labelName
                lg.index = lrLabel.index
                lg.labelName = ''
            }
            if (lg.classDesc != null) {
                mergeGenericLabelsFromRight(lg.classDesc.genericList, right)
            }
        })
    }


    private static List<FLClassDesc.Generic> mergeFlItemLabels(List<FLClassDesc.Generic> itemLabels,
                                                               List<FLClassDesc.Generic> originalGenerics,
                                                               List<FLClassDesc.Generic> labeledGenerics) {

        def mergedFlItemLabels = new ArrayList<FLClassDesc.Generic>()
        for (int j = 0; j < itemLabels.size(); j++) {
            def realLabel = itemLabels[j]
            def findK = -1
            if (realLabel.index != Integer.MAX_VALUE) {
                for (int k = 0; k < originalGenerics.size(); k++) {
                    if (realLabel.labelName == null || realLabel.labelName.trim().length() == 0) {
                        if (realLabel.className == originalGenerics[k].labelName) {
                            findK = k
                            break
                        }
                    } else {
                        if (realLabel.labelName == originalGenerics[k].labelName) {
                            findK = k
                            break
                        }
                    }
                }
            }
            if (findK == -1) {
                def mergedFlItemLabel = realLabel.clone()
                def genericList
                if (mergedFlItemLabel.classDesc != null
                        && (genericList = mergedFlItemLabel.classDesc.genericList) != null
                        && !genericList.isEmpty()) {
                    def newList = mergeFlItemLabels(genericList, originalGenerics, labeledGenerics)
                    genericList.clear()
                    genericList.addAll(newList)
                }
                mergedFlItemLabels.add(mergedFlItemLabel)
            } else {
                def mergedFlItemLabel = labeledGenerics[findK].clone()
                mergedFlItemLabels.add(mergedFlItemLabel)
            }
        }
        mergedFlItemLabels
    }

    @Override
    JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("originalItemList", FLUtil.fromArray(originalItemList))
        jsonObject.put("recordItemList", FLUtil.fromArray(recordItemList))
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
                originalItemList.add(new FLItem(tplId, flClass))
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