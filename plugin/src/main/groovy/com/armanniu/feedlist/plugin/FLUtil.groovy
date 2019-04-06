package com.armanniu.feedlist.plugin

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class FLUtil {

    static JSONObject fromObject(Object o){
        if (o instanceof FLJson){
            return o.getJsonObject()
        }
        return new JSONObject()
    }

    static JSONArray fromArray(List list){
        def array = new JSONArray()
        if (list == null || list.isEmpty()){
            return array
        }
        list.forEach({
            if (it instanceof FLJson){
                array.push(it.getJsonObject())
            } else {
                array.push(it.toString())
            }
        })
        return array
    }
}