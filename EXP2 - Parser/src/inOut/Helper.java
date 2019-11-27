package inOut;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Helper {
	private Properties attributeNames = new Properties();
	private Properties attributeJSONNames = new Properties();
	private Properties usedAttributes = new Properties();
	private Properties airportURIs = new Properties();
	private final List<Integer> usedJSONAttributes = new LinkedList<>();
	
	public void loadAirportURIs(FileInputStream inStream) throws IOException {
		airportURIs.load(inStream);
	}
	
	public void loadAttributeNames(FileInputStream inStream) throws IOException {
		attributeNames.load(inStream);
	}
	
	public void loadJSONNames(FileInputStream inStream) throws IOException {
		attributeJSONNames.load(inStream);
	}
	
	public void loadUsedAttributes(FileInputStream inStream) throws IOException {
		usedAttributes.load(inStream);
	}
	
	public void saveJSONNames(FileOutputStream outStream) throws IOException {
		if(attributeJSONNames.size() == 95) {
			attributeJSONNames.store(outStream,"Attribute names for JSON output.");
		}
	}
	
	public void saveNames(FileOutputStream outStream) throws IOException {
		if(attributeNames.size() == 95) {
			attributeNames.store(outStream,"Attribute names of DDR2 manual.");
		}
	}
	
	public void saveUsedAttributes(FileOutputStream outStream) throws IOException {
		if(usedAttributes.size() == 95) {
			usedAttributes.store(outStream,"Used JSON attributes.");
		}
	}
	
	public void saveAirportURIs(FileOutputStream outStream) throws IOException{
		airportURIs.store(outStream, "DBPedia URIs of ICAO airport codes");
	}
	
	/**
	 * 
	 * @param nr Starts at 1
	 * @return the name of the attribute (DDR2) if the attribute number exists. If not, returns null;
	 */
	public String getAttributeName(int nr) {
		return attributeNames.get(Integer.toString(nr)).toString();
	}
	
	/**
	 * 
	 * @param nr Starts at 1
	 * @return the name of the in the JSON-Output-File if the attribute number exists. If not, returns null;
	 */
	public String getJSONName(int nr) {
		return attributeJSONNames.get(Integer.toString(nr)).toString();
	}
	
	public Boolean isUsed(int nr) {
		return Boolean.valueOf(usedAttributes.get(Integer.toString(nr)).toString());
	}
	
	public String getAirportURI(String airport) {
		return airportURIs.getProperty(airport);
	}
	
	public Properties getJSONAttributes() {
		return this.attributeJSONNames;
	}
	
	public Properties getEXP2Attributes() {
		return this.attributeNames;
	}
	
	public Properties getUsedAttributes() {
		return this.usedAttributes;
	}
	
	public Properties getAirportURIs() {
		return this.airportURIs;
	}

	public List<Integer> getUsedJSONAttributes() {
		return usedJSONAttributes;
	}
}
