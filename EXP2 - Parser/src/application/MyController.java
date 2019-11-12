package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import inOut.Helper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import parser.Parser;

public class MyController {

    @FXML private Button importButton;
    @FXML private Button validateButton;
    @FXML private Button exportButton;
    @FXML private ListView<File> listView = new ListView<>();
    @FXML private Text recordCounter;
    @FXML private TextField getNumber;
    @FXML private CheckBox getAll;
    
    private File directory;
    private Parser parser = new Parser(listView.getItems());
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
    	List<Pair<Integer,Integer>> flaws = this.parser.validate();
    	
    }
    
    @FXML
    void onExport(ActionEvent event) {

    }
    
    @FXML
    void initialize() {
    	this.parser.getCounter().addListener((x,o,n) -> {
    		this.recordCounter.setText(String.valueOf(n.longValue()));
    	});
    	this.recordCounter.setText("0");
    	
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
