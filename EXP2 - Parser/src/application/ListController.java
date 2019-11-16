package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import inOut.Helper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class ListController {
	private abstract class Attribute{
		final SimpleIntegerProperty nr;
		final SimpleStringProperty name;
		
		public Attribute(int nr, String name) {
			this.nr = new SimpleIntegerProperty(nr);
			this.name = new SimpleStringProperty(name);
		}
	}
	
	private class Pair extends Attribute{
		public Pair(int nr, String name) {
			super(nr,name);
			this.name.addListener((x,o,n) -> {
				helper.getEXP2Attributes().setProperty(String.valueOf(this.nr.get()), String.valueOf(n));
			});
		}
	}

	private class Triple extends Attribute{
		final CheckBox checkBox;
		public Triple(int nr, String name, CheckBox checkBox) {
			super(nr,name);
			this.checkBox = checkBox;
			this.name.addListener((x,o,n) -> {
				helper.getJSONAttributes().setProperty(String.valueOf(this.nr.get()), String.valueOf(n));
			});
			this.checkBox.selectedProperty().addListener((x,o,n) -> {
				helper.getUsedAttributes().setProperty(String.valueOf(this.nr.get()), String.valueOf(n));
			});
		}
	}
	
	private Helper helper = null;

    @FXML private TableView<Pair> tableView1;
    @FXML private TableView<Triple> tableView2;
    @FXML private Button saveButton;
    
    @FXML
    void onSave(ActionEvent event) {
    	try {
			helper.saveJSONNames(new FileOutputStream(new File("resources/attributeJSONNames.properties").getAbsolutePath()));
	    	helper.saveNames(new FileOutputStream(new File("resources/attributeNames.properties").getAbsolutePath()));
	    	helper.saveUsedAttributes(new FileOutputStream(new File("resources/usedAttributes.properties").getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void setHelper(Helper helper) {
    	this.helper = helper;
    	fillTable1();
    	fillTable2();
    }
    
    private void fillTable1() {
    	TableColumn<Pair,Number> nr = new TableColumn<>();
    	TableColumn<Pair,String> name = new TableColumn<>();
    	
    	nr.setEditable(false);
    	name.setEditable(true);
    	
    	nr.setCellValueFactory(cellValue -> cellValue.getValue().nr);
    	name.setCellValueFactory(cellValue -> cellValue.getValue().name);
    	
    	name.setCellFactory(TextFieldTableCell.forTableColumn());
    	name.setOnEditCommit(x -> x.getRowValue().name.set(x.getNewValue()));
    	
    	this.tableView1.getColumns().add(nr);
    	this.tableView1.getColumns().add(name);
    	for(int i = 0;i<helper.getEXP2Attributes().size();i++) {
    		this.tableView1.getItems().add(new Pair(i+1,helper.getAttributeName(i+1)));
    	}
    }
    
    private void fillTable2() {
    	TableColumn<Triple,Number> nr = new TableColumn<>();
    	TableColumn<Triple,String> name = new TableColumn<>();
    	TableColumn<Triple,CheckBox> isUsed = new TableColumn<>();
    	
    	nr.setEditable(false);
    	name.setEditable(true);
    	isUsed.setEditable(true);
    	
    	nr.setCellValueFactory(cellValue -> cellValue.getValue().nr);
    	name.setCellValueFactory(cellValue -> cellValue.getValue().name);
    	
    	name.setCellFactory(TextFieldTableCell.forTableColumn());
    	name.setOnEditCommit(x -> x.getRowValue().name.set(x.getNewValue()));
    	
    	this.tableView2.getColumns().add(nr);
    	this.tableView2.getColumns().add(name);
    	this.tableView2.getColumns().add(isUsed);
    	for(int i = 0;i<helper.getEXP2Attributes().size();i++) {
			CheckBox cb = new CheckBox();
			cb.setSelected(Boolean.getBoolean(helper.getUsedAttributes().get(Integer.toString(i+1)).toString()));
    		this.tableView2.getItems().add(new Triple(i+1,helper.getAttributeName(i+1),cb));
    	}
    }
}
