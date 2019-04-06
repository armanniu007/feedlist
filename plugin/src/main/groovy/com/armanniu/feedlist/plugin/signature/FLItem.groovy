package com.armanniu.feedlist.plugin.signature

import com.armanniu.feedlist.plugin.FLJson
import com.armanniu.feedlist.plugin.FLUtil
import org.json.simple.JSONObject

class FLItem implements FLJson {

    private String tplId
    private FLClass flClass
    private FLClassDesc.Desc itemDesc

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

    FLClassDesc.Desc getItemDesc() {
        return itemDesc
    }

    void setItemDesc(FLClassDesc.Desc itemDesc) {
        this.itemDesc = itemDesc
    }

    @Override
    JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("tplId", tplId)
        jsonObject.put("flClass", FLUtil.fromObject(flClass))
        jsonObject.put("itemDesc", FLUtil.fromObject(itemDesc))
        return jsonObject
    }

    @Override
    String toString() {
        return getJsonObject().toString()
    }
}