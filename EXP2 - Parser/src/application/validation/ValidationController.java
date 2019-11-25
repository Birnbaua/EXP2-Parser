package application.validation;

import java.util.List;

import basics.ErrorCategory;
import basics.ErrorLog;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ValidationController {

	@FXML Label flaws;
	@FXML TableView<ErrorLog> tableView;
	
	public void addList(List<ErrorLog> list) {
		tableView.getItems().addAll(list);
		flaws.setText(String.valueOf(list.size()));
	}
	
	public ObservableList<ErrorLog> getList() {
		return this.tableView.getItems();
	}
	
	@FXML
	void initialize() {
		
		TableColumn<ErrorLog,String> flightId = new TableColumn<>();
		TableColumn<ErrorLog,String> attribute = new TableColumn<>();
		TableColumn<ErrorLog,ErrorCategory> category = new TableColumn<>();
		
		flightId.setCellValueFactory(value -> value.getValue().getFLIGHT_ID());
		attribute.setCellValueFactory(value -> value.getValue().getATTRIBUTE_NAME());
		category.setCellValueFactory(value -> value.getValue().getCATEGORY());
		
		flightId.setText("Flight ID");
		attribute.setText("Attribute");
		category.setText("Category");
		
		this.tableView.getColumns().add(flightId);
		this.tableView.getColumns().add(attribute);
		this.tableView.getColumns().add(category);
		this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
}
