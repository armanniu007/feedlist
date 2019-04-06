package com.armanniu.feedlist.plugin.signature

import com.armanniu.feedlist.plugin.FLJson
import com.armanniu.feedlist.plugin.FLUtil
import org.json.simple.JSONObject

class FLClass implements FLJson{

    /**
     * class名称，包含命名空间
     */
    private String className
    /**
     * class 签名 <A:Ljava/lang/Object;T::Lcom/armanniu/component/FLItemData;B::Lcom/armanniu/feedlist/Data<TT;>;>Lcom/armanniu/component/BaseFLItem<Lcom/armanniu/component/FLItemData;>;
     */
    private String signature
    /**
     * 父类class名称
     */
    private String superName
    /**
     * class 实现的接口
     */
    private String[] interfaces
    /**
     * class 文件位置
     */
    private String classFilePath

    FLClass(String className, String signature) {
        this(className,signature,null,null,null)
    }

    FLClass(String className, String signature, String superName, String[] interfaces,String classFilePath) {
        this.className = className
        this.signature = signature
        this.superName = superName
        this.interfaces = interfaces
        this.classFilePath = classFilePath
    }

    String getClassName() {
        return className
    }

    String getSignature() {
        return signature
    }

    String getSuperName() {
        return superName
    }

    String[] getInterfaces() {
        return interfaces
    }

    String getClassFilePath() {
        return classFilePath
    }



    @Override
    JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("className",className)
        jsonObject.put("signature",signature)
        jsonObject.put("superName",superName)
        jsonObject.put("interfaces", FLUtil.fromArray(interfaces == null ? null : Arrays.asList(interfaces)))
        jsonObject.put("classFilePath",classFilePath)
        return jsonObject
    }

    @Override
    public String toString() {
        return getJsonObject().toString()
    }
}