package inOut;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Helper {
	private Properties attributeNames = new Properties();
	private Properties attributeJSONNames = new Properties();
	
	public void loadAttributeNames(FileInputStream inStream) throws IOException {
		attributeNames.load(inStream);
	}
	
	public void loadJSONNames(FileInputStream inStream) throws IOException {
		attributeJSONNames.load(inStream);
	}
	
	public void saveJSONNames(FileOutputStream outStream) throws IOException {
		if(attributeJSONNames.size() == 95) {
			attributeJSONNames.store(outStream,"Attribute names for JSON output.");
		}
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
		return attributeNames.get(Integer.toString(nr)).toString();
	}
}
