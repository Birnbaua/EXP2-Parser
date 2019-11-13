package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import inOut.Helper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import parser.Parser;

public class MyController {

    @FXML private Button importButton;
    @FXML private Button validateButton;
    @FXML private Button exportButton;
    @FXML private ListView<File> listView;
    @FXML private Text recordCounter;
    @FXML private TextField getNumber;
    @FXML private CheckBox getAll;
    
    private File directory;
    private Parser parser = new Parser();
    private Helper helper = new Helper();

    @FXML
    void onImport(ActionEvent event) {
    	Stage stage = new Stage();
		FileChooser fileChooser = new FileChooser();
		if(directory == null) {
			directory = fileChooser.getInitialDirectory();
		}
		fileChooser.setInitialDirectory(directory);
		fileChooser.setTitle("Import txt files");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text-Files (*.exp2)", "*.exp2"));
		List<File> files = fileChooser.showOpenMultipleDialog(stage);
		if(files != null) {
			this.listView.getItems().addAll(files);
			this.directory = files.get(0).getParentFile();
		}
    }
    
    @FXML
    void onValidate(ActionEvent event) {
    	ProgressBar pb = new ProgressBar();
    	List<Pair<String,Integer>> flaws = this.parser.validate(this.listView.getItems(),pb);
    	System.out.println(flaws.size());
    }
    
    @FXML
    void onExport(ActionEvent event) {
    	parser.refreshCounter(listView.getItems(), new LinkedList<>());
    }
    
    @FXML
    void initialize() {
    	this.parser.getCounter().addListener((x,o,n) -> {
    		this.recordCounter.setText(String.valueOf(n.intValue()));
    	});
    	this.recordCounter.setText("0");
    	
    	this.listView.getItems().addListener((ListChangeListener<File>) (x) -> {
    		SimpleIntegerProperty counter = parser.getCounter();
			counter.set(0);
			if(x.next()) {
				parser.refreshCounter(x.getAddedSubList(),x.getRemoved());
			}
		});
    	
    	//load attribute names
    	try {
			helper.loadAttributeNames(new FileInputStream(new File("resources/attributeNames.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
		
    	//load names of attributes in JSON file
    	try {
			helper.loadJSONNames(new FileInputStream(new File("resources/attributeJSONNames.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
    	
    	
    }
}
