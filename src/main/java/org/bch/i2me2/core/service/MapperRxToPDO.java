package org.bch.i2me2.core.service;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * IME-28
 * Mapping between a given RxJSON string to PDO XML
 * Created by CH176656 on 3/10/2015.
 */
public class MapperRxToPDO extends Mapper{

    public static String RX_PATIENTSEGMENT= "PatientSegments";
    public static String RX_HISTORYSEGMENT = "RxHistorySegments";
    public static String RX_ORDERS = "orders";
    public static String RX_RXD = "rxd";
    public static String RX_ORC = "orc";


    public String getPDOXML(String jsonString) throws I2ME2Exception {
        String result=null;
        try {
            result = doMap(jsonString);
        } catch (Exception e) {
            throw new I2ME2Exception(e.getMessage(), e);
        }
        return result;
    }

    // Override methods from Mapper
    @Override
    public List<JSONObject> getJSONObjects(JSONObject root) throws JSONException {
        List<JSONObject> listJSON = new ArrayList<>();
        JSONObject patientSegments = root.getJSONObject(RX_PATIENTSEGMENT);
        JSONObject rxHistorySegments = root.getJSONObject(RX_HISTORYSEGMENT);
        listJSON.add(patientSegments);
        listJSON.add(rxHistorySegments);
        return listJSON;
    }

    @Override
    public JSONArray getJSONArray(JSONObject root) throws JSONException {
        JSONObject rxHistorySegments = root.getJSONObject(RX_HISTORYSEGMENT);
        return rxHistorySegments.getJSONArray(RX_ORDERS);
    }

    @Override
    public List<JSONObject> getJSONObjectsInArray(JSONObject jsonObjectInArray) throws JSONException {
        List<JSONObject> listJSON = new ArrayList<>();
        JSONObject rdx = jsonObjectInArray.getJSONObject(RX_RXD);
        JSONObject orc = jsonObjectInArray.getJSONObject(RX_ORC);
        listJSON.add(rdx);
        listJSON.add(orc);
        return listJSON;
    }
}

