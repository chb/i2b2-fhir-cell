package org.bch.i2me2.core.util.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that implements a generic mapping between RXCOnnect JSON and XML PDO 
 * @author CH176656
 *
 */
public class Mapper {
	
	// Name of the xml file that acts as template
	//public static String XML_MAP_FILE_NAME="xmlpdoTemplate.xml";
	public static String XML_MAP_FILE_NAME="xmlpdoTemplate_CurrentMapping.xml";
	
	// Separator
	public static String PREPOST = "F_F";
	
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
	
	// Keys of RXConnect JSON
	public static String RX_PATIENTSEGMENT= "PatientSegments";
	public static String RX_HISTORYSEGMENT = "RxHistorySegments";
	public static String RX_ORDERS = "orders";
	public static String RX_RXD = "rxd";
	public static String RX_ORC = "orc";
		
	
	private JSONObject jsonRoot =null;
	
	Map<XmlPdoTag, String> mapTemplate = new HashMap<XmlPdoTag, String>();
	Map<XmlPdoTag, String> mapResult = new HashMap<XmlPdoTag, String>();
	
	public static void main(String [] args) throws Exception {
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
	}
	
	/**
	 * Returns the xml elements for the given tag set
	 * @param tag
	 * @return
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
		StringBuffer out = new StringBuffer();
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
	
	private String mapElements(JSONObject json, String baseXML) throws JSONException {
		String out = baseXML;
		Iterator<?> keys = json.keys();
		while( keys.hasNext() ){
            String key = (String)keys.next();
            try {
            	String value = json.getString(key);
            	out = out.replaceAll(PREPOST+key+PREPOST, value);
            	
            } catch (Exception e) {
            	try {
            		// We check if it is a long value instead
            		Long value = json.getLong(key);
            		out = out.replaceAll(PREPOST+key+PREPOST, value.toString());
            	} catch (Exception ee) {
            		// Its neither, so, we don't really need to do anything
            	}
            }
            
		}
		return out;
	}
	
	private void loadXMLTemplate() throws IOException{
		InputStream in = this.getClass().getResourceAsStream(XML_MAP_FILE_NAME);
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
		String part = xmlString.substring(init+tagInit.length(), end);
		return part;
	}
}
