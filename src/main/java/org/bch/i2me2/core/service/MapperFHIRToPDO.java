package org.bch.i2me2.core.service;

import org.bch.i2me2.core.util.mapper.Mapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CH176656 on 4/10/2015.
 */
public class MapperFHIRToPDO extends Mapper {


    @Override
    protected JSONArray getJSONArray(JSONObject root) throws JSONException {
        JSONObject rxHistorySegments = root.getJSONObject("");
        return rxHistorySegments.getJSONArray("");
    }
}
