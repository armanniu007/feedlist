package com.armanniu.feedlist.plugin.signature

import com.armanniu.feedlist.plugin.FLJson
import com.armanniu.feedlist.plugin.FLUtil
import org.json.simple.JSONObject

/**
 * 类描述
 *
 * map: com/armanniu/feedlist/StringFLItem<A:Ljava/lang/Object;T::Lcom/armanniu/feedlist/FLItemData;B::Lcom/armanniu/feedlist/Data<TT;>;>
 *
 * to: FLClass<[{labelName}:]{type}{FLClassDesc}...>
 *
 */
class FLClassDesc implements FLConstant, Cloneable, FLJson {
    private static def debug = true
    /**
     * 描述所属的类
     */
    private final FLClass flClass
    /**
     * 泛型修饰集合
     */
    private final List<Generic> genericList
    /**
     * 父类或接口的类描述
     */
    private List<FLClassDesc> labelGenericList

    private FLClassDesc(FLClass flClass, List<Generic> genericList) {
        this.flClass = flClass
        this.genericList = genericList
    }

    FLClass getFlClass() {
        return flClass
    }

    List<Generic> getGenericList() {
        return genericList
    }

    List<FLClassDesc> getLabelGenericList() {
        if (labelGenericList == null) {
            labelGenericList = getSuperFLClassList(flClass)
        }
        return labelGenericList
    }

    @Override
    FLClassDesc clone() throws CloneNotSupportedException {
        def clone
        if (genericList != null) {
            def cloneDescList = new ArrayList<Generic>()
            genericList.forEach({ cloneDescList.add(it.clone()) })
            clone = new FLClassDesc(flClass, cloneDescList)
        } else {
            clone = new FLClassDesc(flClass, null)
        }
        if (labelGenericList != null) {
            def cloneSuperClassDescList = new ArrayList<FLClassDesc>()
            labelGenericList.forEach({ cloneSuperClassDescList.add(it.clone()) })
            clone.labelGenericList = cloneSuperClassDescList
        }
        return clone
    }

    static FLClassDesc create(FLClass flClass) {
        if (flClass == null) {
            return null
        }
        if (debug) {
            println("create ${flClass.toString()}")
        }
        def descList = createDescList(flClass.signature)
        def classDesc = new FLClassDesc(flClass, descList)
        return classDesc
    }

    private static List<Generic> createDescList(String signature) {
        if (signature == null || signature.length() == 0) {
            return null
        }
        if (debug) {
            println("createDescList ${signature}")
        }
        def classSign = getClassSign(signature)
        if (classSign == null || classSign.length() == 0) {
            return null
        }
        def array = getSignArray(classSign)
        if (array == null || array.isEmpty()) {
            return null
        }
        def list = new ArrayList<Generic>()
        for (int i = 0; i < array.size(); i++) {
            def desc = createDesc(array[i], i)
            if (desc != null) {
                list.add(desc)
            }
        }
        return list
    }

    private static Generic createDesc(String sign, int index) {
        if (debug) {
            println("createDesc " + sign)
        }
        def spl
        def isInterface = false
        if (sign.contains("::")) {
            spl = sign.split("::")
            isInterface = true
        } else if (sign.contains(":")) {
            spl = sign.split(":")
            isInterface = false
        } else {
            spl = new String[2]
            spl[0] = ''
            spl[1] = sign
        }
        if (spl == null || spl.length != 2) {
            throw new IllegalAccessException("parse ${sign} error to array")
        }
        if (spl[1] == null || spl[1].length() == 0) {
            throw new IllegalAccessException("parse ${sign} error to get class name")
        }
        def labelName = spl[0]
        def type = spl[1].charAt(0).toString()
        def className = spl[1].substring(1)
        if (type != TYPE_SIGN && type != TYPE_CLASS) {
            throw new IllegalAccessException("parse ${sign} error to get class type ${className}:${type}")
        }
        def flClass
        def newClassName
        if (className.indexOf("<") <= 0) {
            flClass = new FLClass(className, null)
            newClassName = className
        } else {
            def signIndex = className.indexOf("<")
            newClassName = className.substring(0, signIndex)
            flClass = new FLClass(newClassName, className.substring(signIndex))
        }
        def desc = new Generic(create(flClass), type, newClassName, isInterface, labelName, index)
        if (debug) {
            println("createDesc " + desc.toString())
        }
        return desc
    }

    /**
     * <A::Lcom/armanniu/feedlist/Data;T::Lcom/armanniu/component/FLItemData;B::Lcom/armanniu/feedlist/Data<TT;>;>
     * @param sign
     * @return
     */
    private static List<String> getSignArray(String sign) {
        if (sign == null || sign.length() == 0) {
            return null
        }
        def list = new ArrayList<String>()
        def l = sign.length()
        def deep = 0
        def i = 0
        def index = 0
        for (; i < l; i++) {
            if ('<' == sign.charAt(i).toString()) {
                deep++
                continue
            }
            if ('>' == sign.charAt(i).toString()) {
                deep--
            }
            if (';' == sign.charAt(i).toString()) {
                if (deep == 1) {
                    def subSign = sign.substring(index + 1, i)
//                    //去除泛型
//                    if (subSign.indexOf('<') > 0) {
//                        subSign = subSign.substring(0, subSign.indexOf('<'))
//                    }
                    list.add(subSign)
                    index = i
                }
            }
            if (deep == 0) {
                break
            }
        }
        return list
    }

    private static String getClassSign(String sign) {
        if (debug) {
            println("getClassSign " + sign)
        }
        if (sign == null || sign.length() == 0) {
            return null
        }
        if ('<' != sign.charAt(0).toString()) {
            return null
        }
        def l = sign.length()
        def deep = 0
        def i = 0
        for (; i < l; i++) {
            if ('<' == sign.charAt(i).toString()) {
                deep++
                continue
            }
            if ('>' == sign.charAt(i).toString()) {
                deep--
            }
            if (deep == 0) {
                break
            }
        }
        if (i + 1 >= l) {
            return sign
        }
        sign = sign.substring(0, i + 1)
        if (debug) {
            println("getClassSign " + sign)
        }
        return sign
    }

    private static List<FLClassDesc> getSuperFLClassList(FLClass flClass) {
        def sign = flClass.signature
        if (debug) {
            println("getSuperFLClassList " + flClass)
        }
        def list = new ArrayList<FLClassDesc>()
        if (sign != null && sign.length() > 0) {
            if ('<' == sign.charAt(0).toString()) {
                def l = sign.length()
                def deep = 0
                def i = 0
                for (; i < l; i++) {
                    if ('<' == sign.charAt(i).toString()) {
                        deep++
                        continue
                    }
                    if ('>' == sign.charAt(i).toString()) {
                        deep--
                    }
                    if (deep == 0) {
                        break
                    }
                }
                if (i + 1 < l) {
                    sign = sign.substring(i + 1)
                } else {
                    sign = ""
                }
            }
            if (debug) {
                println("getSuperFLClassList " + sign + "   [2]")
            }
            def l = sign.length()
            def i = 0
            def deep = 0
            def firstDeepFirstIndex = 0
            for (; i < l; i++) {
                if (deep == 0 && ';' == sign.charAt(i).toString()) {
                    def className = sign.substring(firstDeepFirstIndex + 1, i)
                    FLClass childFlClass
                    if (className.indexOf("<") <= 0) {
                        childFlClass = new FLClass(className, null)
                    } else {
                        def index = className.indexOf("<")
                        childFlClass = new FLClass(className.substring(0, index), className.substring(index))
                    }
                    list.add(create(childFlClass))
                    firstDeepFirstIndex = i + 1
                    continue
                }
                if ('<' == sign.charAt(i).toString()) {
                    deep++
                } else if ('>' == sign.charAt(i).toString()) {
                    deep--
                }
            }
        }
        if (flClass.superName != null && flClass.superName.length() > 0) {
            def superClass = list.find({ it.flClass.className == flClass.superName })
            if (superClass == null) {
                list.add(create(new FLClass(flClass.superName, null)))
            }
        }
        if (flClass.interfaces != null && flClass.interfaces.length > 0) {
            for (int j = 0; j < flClass.interfaces.length; j++) {
                def interfaceClass = list.find({ that -> that.flClass.className == flClass.interfaces[j] })
                if (interfaceClass == null) {
                    list.add(create(new FLClass(flClass.interfaces[j], null)))
                }
            }
        }
        return list
    }

    @Override
    JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("flClass", FLUtil.fromObject(flClass))
        jsonObject.put("genericList", FLUtil.fromArray(genericList))
        jsonObject.put("labelGenericList", FLUtil.fromArray(labelGenericList))
        return jsonObject
    }

    @Override
    public String toString() {
        return getJsonObject().toString()
    }


    static class Generic implements Cloneable, FLJson {

        private FLClassDesc classDesc
        private String type
        private String className
        private boolean isInterface
        private String labelName
        private int index

        Generic(FLClassDesc classDesc, String type, String className, boolean isInterface, String labelName, int index) {
            this.classDesc = classDesc
            this.type = type
            this.className = className
            this.isInterface = isInterface
            this.labelName = labelName
            this.index = index
        }

        FLClassDesc getClassDesc() {
            return classDesc
        }

        String getType() {
            return type
        }

        String getClassName() {
            return className
        }

        void setClassName(String className) {
            this.className = className
        }

        boolean getIsInterface() {
            return isInterface
        }

        String getLabelName() {
            return labelName
        }

        void setLabelName(String labelName) {
            this.labelName = labelName
        }

        int getIndex() {
            return index
        }

        void setIndex(int index) {
            this.index = index
        }

        @Override
        Generic clone() throws CloneNotSupportedException {
            def clone
            if (classDesc == null) {
                clone = new Generic(null, type, className, isInterface, labelName, index)
            } else {
                clone = new Generic(classDesc.clone(), type, className, isInterface, labelName, index)
            }
            return clone
        }

        @Override
        JSONObject getJsonObject() {
            JSONObject jsonObject = new JSONObject()
            jsonObject.put("type", type)
            jsonObject.put("className", className)
            jsonObject.put("isInterface", isInterface)
            jsonObject.put("labelName", labelName)
            jsonObject.put("index", index)
            jsonObject.put("classDesc", FLUtil.fromObject(classDesc))
            return jsonObject
        }


        @Override
        public String toString() {
            return getJsonObject().toString()
        }
    }
}