package org.bch.i2me2.core.util.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that implements a generic mapping between JSON-like and XML PDO
 * JSON structure is {a1:object, a2:object .... b:array[ {c1:object, c2:object ...}]}
 * @author CH176656
 *
 */
public abstract class Mapper {
	
	// Name of the xml file that acts as template
	//public static String XML_MAP_FILE_NAME="xmlpdoTemplate.xml";

	private String xmlMapFileTemplate="xmlpdoTemplate.xml";
	
	// delimiter between fields
	private String prePost = "F_F";
	
	// Tab: 4 blank spaces
	public static String TAB = "    "; 
	/**
	 * The enum class for the xml pdo set tags 
	 * @author CH176656
	 *
	 */
	public static enum XmlPdoTag {
		TAG_OBSERVATIONS ("observation_set"),
		TAG_EVENTS ("event_set"),
		TAG_CONCEPTS ("concept_set"),
		TAG_EIDS ("eid_set"),
		TAG_PIDS ("pid_set"),
		TAG_MODIFIERS ("modifier_set"),
		TAG_PATIENTS ("patient_set");
		
		private String tagValue;
		XmlPdoTag(String tagValue) {
			this.tagValue = tagValue;
		}
		
		public String toString() {
			return this.tagValue;
		}
	}

    /*
	// Keys of RXConnect JSON
	public static String RX_PATIENTSEGMENT= "PatientSegments";
	public static String RX_HISTORYSEGMENT = "RxHistorySegments";
	public static String RX_ORDERS = "orders";
	public static String RX_RXD = "rxd";
	public static String RX_ORC = "orc";
	*/
	
	private JSONObject jsonRoot =null;
	
	Map<XmlPdoTag, String> mapTemplate = new HashMap<>();
	Map<XmlPdoTag, String> mapResult = new HashMap<>();
	
	public static void main(String [] args) throws Exception {
        /*
		InputStream in = Mapper.class.getResourceAsStream("jsonExample.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer sBuffer = new StringBuffer();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sBuffer.append(line).append('\n');
			}
		} catch(Exception e) {
			e.printStackTrace();
			
		} finally {
			in.close();
		}
		
		Mapper a = new Mapper();
		try {
			String result = a.doMap(sBuffer.toString());
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	public Mapper() {
		mapTemplate.put(XmlPdoTag.TAG_OBSERVATIONS, null);
		mapTemplate.put(XmlPdoTag.TAG_EVENTS, null);
		mapTemplate.put(XmlPdoTag.TAG_CONCEPTS, null);
		mapTemplate.put(XmlPdoTag.TAG_EIDS, null);
		mapTemplate.put(XmlPdoTag.TAG_PIDS, null);
		mapTemplate.put(XmlPdoTag.TAG_MODIFIERS, null);
		mapTemplate.put(XmlPdoTag.TAG_PATIENTS, null);
		
		mapResult.put(XmlPdoTag.TAG_OBSERVATIONS, null);
		mapResult.put(XmlPdoTag.TAG_EVENTS, null);
		mapResult.put(XmlPdoTag.TAG_CONCEPTS, null);
		mapResult.put(XmlPdoTag.TAG_EIDS, null);
		mapResult.put(XmlPdoTag.TAG_PIDS, null);
		mapResult.put(XmlPdoTag.TAG_MODIFIERS, null);
		mapResult.put(XmlPdoTag.TAG_PATIENTS, null);
		
	}
	
	/**
	 * Perform the map
	 * @param jsonInput			The RXConnect JSON-formatted string
	 * @return					The entire XML PDO as String
	 * @throws IOException		If there is a problem reading the xml template
	 * @throws JSONException	If there is a problem parsing the json 
	 */
    public String doMap(String jsonInput) throws IOException, JSONException {
        loadXMLTemplate();
        if (jsonInput==null) {
            throw new JSONException("Input cannot be null");
        }
        this.jsonRoot = new JSONObject(jsonInput);
        List<JSONObject> jsonObjects = getJSONObjects(this.jsonRoot);
        JSONArray jsonArray = getJSONArray(this.jsonRoot);
        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject jsonObjectInArray = jsonArray.getJSONObject(i);
            List<JSONObject> jsonObjectsInArray = getJSONObjectsInArray(jsonObjectInArray);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_OBSERVATIONS);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_EVENTS);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_CONCEPTS);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_PATIENTS);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_PIDS);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_EIDS);
            performElementMap(jsonObjects, jsonObjectsInArray, XmlPdoTag.TAG_MODIFIERS);

        }
        return buildXMLOutput();
    }
    /*
	public String doMap(String jsonInput) throws IOException, JSONException {

		loadXMLTemplate();
		this.jsonRoot = new JSONObject(jsonInput);
		JSONObject patientSegments = jsonRoot.getJSONObject(RX_PATIENTSEGMENT);
		JSONObject rxHistorySegments = jsonRoot.getJSONObject(RX_HISTORYSEGMENT);
		JSONArray orders = rxHistorySegments.getJSONArray(RX_ORDERS);

		// Map medications
		for (int i = 0; i < orders.length(); i++) {
			JSONObject order = orders.getJSONObject(i);
			JSONObject rdx = order.getJSONObject(RX_RXD);
			JSONObject orc = order.getJSONObject(RX_ORC);

			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_OBSERVATIONS);
			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_EVENTS);
			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_CONCEPTS);
			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_PATIENTS);
			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_PIDS);
			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_EIDS);
			mapComplex(patientSegments, rxHistorySegments, rdx, orc, XmlPdoTag.TAG_MODIFIERS);
		}
		return buildXMLOutput();
	}*/


    /**
     * Returns the list ob jsonObjects that are in the root of the element, so, that are not in the array
     * It will be called only once
     * @return the list of jsonObjects
     */
    public abstract List<JSONObject> getJSONObjects(JSONObject root) throws JSONException;

    /**
     * Returns the jsonArray
     * It will be called only once
     * @return The JSONArray instance
     */
    public abstract JSONArray getJSONArray(JSONObject root) throws JSONException;

    /**
     * Returns the list of jsonObjects in an object of the array
     * It will be called one time for each object of the array
     * @return The List of JSONObjects
     */
    public abstract List<JSONObject> getJSONObjectsInArray(JSONObject jsonObjectInArray) throws JSONException;

	/**
	 * Returns the xml elements for the given tag set
	 * @param tag The tag
	 * @return the XML string
	 * @throws MapperException
	 */
    public String getXMLElements(XmlPdoTag tag) throws MapperException {
		String output = this.mapResult.get(tag);
		if (output == null) {
			throw new MapperException("Fatal Error: No mapping for the given tag");
		}
		return output;
	}
	
	private String buildXMLOutput() {
		StringBuilder out = new StringBuilder();
		Set<XmlPdoTag> keys = mapTemplate.keySet();
		for(XmlPdoTag keyTag:keys) {
			String key = keyTag.toString();
			String tagInit ="<" + key +">";
			String tagEnd = "</" + key +">";
			out.append(TAB).append(tagInit).append('\n');
			out.append(mapResult.get(keyTag)).append('\n');
			out.append(TAB).append(tagEnd).append('\n');
		}
		return out.toString();
	}

    private void performElementMap(List<JSONObject> jsonObjects, List<JSONObject> jsonObjectsInArray, XmlPdoTag tag) throws JSONException {
        String out = mapTemplate.get(tag);
        for(JSONObject jsonObj:jsonObjects) {
            out = mapElements(jsonObj, out);
        }
        for(JSONObject jsonObj:jsonObjectsInArray) {
            out = mapElements(jsonObj, out);
        }
        out = out + '\n';
        String rem = mapResult.get(tag);
        if (rem!=null) {
            out = out + rem;
        }
        mapResult.put(tag, out);
    }
/*
	private void mapComplex(JSONObject patientSegments, JSONObject rxHistorySegments, JSONObject rdx, JSONObject ord, XmlPdoTag tag) throws JSONException {
		String aux = mapElementsExtended(patientSegments, rxHistorySegments, rdx, ord, mapTemplate.get(tag));
		aux = aux + '\n';
		String rem = mapResult.get(tag);
		if (rem!=null) {
			aux = aux + rem;
		}
		mapResult.put(tag, aux);
	}
	
	private String mapElementsExtended(JSONObject json1, JSONObject json2, JSONObject json3, JSONObject json4, String baseXML) throws JSONException{
		String out = mapElements(json1, baseXML);
		out = mapElements(json2, out);
		out = mapElements(json3, out);
		out = mapElements(json4, out);
		return out;
	}
*/
	private String mapElements(JSONObject json, String baseXML) throws JSONException {
		String out = baseXML;
		Iterator<?> keys = json.keys();
		while( keys.hasNext() ){
            String key = (String)keys.next();
            try {
            	String value = json.getString(key);
            	out = out.replaceAll(prePost+key+prePost, value);
            	
            } catch (Exception e) {
            	try {
            		// We check if it is a long value instead
            		Long value = json.getLong(key);
            		out = out.replaceAll(prePost+key+prePost, value.toString());
            	} catch (Exception ee) {
            		// Its neither, so, we don't really need to do anything
            	}
            }
		}
		return out;
	}
	
	private void loadXMLTemplate() throws IOException{
        InputStream in = Mapper.class.getResourceAsStream(xmlMapFileTemplate);
        if (in==null) {
            throw new IOException("Template File not found:" + xmlMapFileTemplate);
        }
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sBuffer = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sBuffer.append(line).append('\n');
			}
		} catch(Exception e) {
			e.printStackTrace();
			
		} finally {
			in.close();
		}
		String completeXMLTemplate = sBuffer.toString();
		Set<XmlPdoTag> keys = mapTemplate.keySet();
		for(XmlPdoTag keyTag:keys) {
			String key = keyTag.toString();
			String aux = getPart(key, completeXMLTemplate);
			mapTemplate.put(keyTag, aux);
		}
	}
	
	private String getPart(String xmlTag, String xmlString) {
		String tagInit ="<" + xmlTag +">";
		String tagEnd = "</" + xmlTag +">";
		int init = xmlString.indexOf(tagInit);
		int end = xmlString.indexOf(tagEnd);
		return xmlString.substring(init+tagInit.length(), end);
	}

    public void setXmlMapFileTemplate(String xmlMapFileTemplate) {
        this.xmlMapFileTemplate = xmlMapFileTemplate;
    }
    public String getXmlMapFileTemplate() {
        return this.xmlMapFileTemplate;
    }

    public void setPrePost(String prePost) {
        this.prePost = prePost;
    }

    public String getPrePost(){
        return this.prePost;
    }
}
