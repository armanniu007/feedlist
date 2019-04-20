package com.armanniu.feedlist.plugin.signature

import com.armanniu.feedlist.plugin.FLJson
import com.armanniu.feedlist.plugin.FLUtil
import org.json.simple.JSONObject

class FLItem implements FLJson {

    private String tplId
    private FLClass flClass
    private List<FLClassDesc.Generic> labelItemGenericList

    FLItem(String tplId, FLClass flClass) {
        this.tplId = tplId
        this.flClass = flClass
    }

    String getTplId() {
        return tplId
    }

    FLClass getFlClass() {
        return flClass
    }

    List<FLClassDesc.Generic> getLabelItemGenericList() {
        return labelItemGenericList
    }

    void setLabelItemGenericList(List<FLClassDesc.Generic> labelItemGenericList) {
        this.labelItemGenericList = labelItemGenericList
    }

    @Override
    JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("tplId", tplId)
        jsonObject.put("flClass", FLUtil.fromObject(flClass))
        jsonObject.put("labelItemGenericList", FLUtil.fromArray(labelItemGenericList))
        return jsonObject
    }

    @Override
    String toString() {
        return getJsonObject().toString()
    }
}