package com.armanniu.feedlist.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class FLTransform extends Transform {

    private static String TAG = "FeedListPlugin"

    Project project
    def supportScopes = Sets.immutableEnumSet(QualifiedContent.Scope.SUB_PROJECTS)

    /**
     * 构造函数，我们将Project保存下来备用
     * @param project
     */
    FLTransform(Project project) {
        this.project = project
    }

    /**
     * 设置我们自定义的Transform对应的Task名称
     * @return
     */
    @Override
    String getName() {
        return "FeedListTransform"
    }

    /**
     * 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
     * 这样确保其他类型的文件不会传入
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定Transform的作用范围
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        println("${TAG} transform start")
        /*
         * Transform的inputs有两种类型，
         * 一种是目录， DirectoryInput
         * 一种是jar包，JarInput
         * 要分开遍历
         */
        def classParser = new FLClassParser(this.project)
        inputs.each { TransformInput input ->
            //解析目录文件，对feed模板进行统计
            input.directoryInputs.each {
                println("|\n|\tdir == ${it}\n|")
                classParser.fromDirector(it.file)
                // 获取output目录
                def dest = outputProvider.getContentLocation(it.name,
                        it.contentTypes, supportScopes,
                        Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(it.file, dest)
            }

            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                // 重命名输出文件（同目录copyFile会冲突）
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                //生成输出路径
                def outFile = outputProvider.getContentLocation(md5Name, jarInput.contentTypes, supportScopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, outFile)
                classParser.fromJar(jarInput)
            }
        }
        try {
            println("-----------------------------------")
            println(classParser.toString())
            println("-----------------------------------")
            classParser.parseFlItems()
            println("-----------------------------------")
            println(classParser.toString())
            println("-----------------------------------")
        } catch (Throwable throwable) {
            throwable.printStackTrace()
            throw throwable
        }
    }
}