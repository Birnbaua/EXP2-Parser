package basics;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class ErrorLog {
	private final SimpleStringProperty FLIGHT_ID;
	private final SimpleStringProperty ATTRIBUTE_NAME;
	private final SimpleObjectProperty<ErrorCategory> CATEGORY;
	
	public ErrorLog(String id, String attribute, ErrorCategory err) {
		this.FLIGHT_ID = new SimpleStringProperty(id);
		this.ATTRIBUTE_NAME = new SimpleStringProperty(attribute);
		this.CATEGORY = new SimpleObjectProperty<>(err);
	}

	public SimpleStringProperty getFLIGHT_ID() {
		return FLIGHT_ID;
	}

	public SimpleStringProperty getATTRIBUTE_NAME() {
		return ATTRIBUTE_NAME;
	}

	public SimpleObjectProperty<ErrorCategory> getCATEGORY() {
		return CATEGORY;
	}
}
