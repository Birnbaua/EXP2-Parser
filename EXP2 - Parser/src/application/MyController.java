package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.controlsfx.dialog.ProgressDialog;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import parser.Parser;

public class MyController {

    @FXML private Button importButton;
    @FXML private Button validateButton;
    @FXML private Button exportButton;
    @FXML private Button editButton;
    @FXML private ListView<File> listView;
    @FXML private Text recordCounter;
    @FXML private TextField getNumber;
    @FXML private CheckBox getAll;
    
    private File directory;
    private Parser parser = new Parser();
    ExecutorService exec = Executors.newSingleThreadScheduledExecutor();

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
    	Task<List<Pair<String,Integer>>> worker = parser.validate(this.listView.getItems());
    	ProgressDialog dialog = new ProgressDialog(worker);
    	dialog.setTitle("Validating files.");
    	dialog.setContentText("The program is validating all listed files now.");
    	exec.submit(worker);
    	dialog.showAndWait();
    	
    	//check number of invalid records
    	System.out.println(worker.getValue().size());
    }
    
    @FXML
    void onExport(ActionEvent event) {
    	Stage stage = new Stage();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if(directory != null) {
			directoryChooser.setInitialDirectory(directory);
		}
		directoryChooser.setTitle("Choose Export Directory");
		File file = directoryChooser.showDialog(stage);
		
		Task<Boolean> worker = parser.exportAsJson(this.listView.getItems(), new File(file.getAbsoluteFile() + "//converted.json"), 
				getAll.isSelected() ? this.parser.getCounter().longValue() : Long.parseLong(this.getNumber.getText()));
    	ProgressDialog dialog = new ProgressDialog(worker);
    	dialog.setTitle("Exporting files.");
    	dialog.setContentText("The program is exporting all listed files now.");
    	exec.submit(worker);
    	dialog.showAndWait();
    }
    
    @FXML
    void onEdit() {
    	ListController listController = null;
    	Parent root;
    	FXMLLoader loader;
    	try {
    		loader = new FXMLLoader(Main.class.getResource("Attributes.fxml"));
    		root = loader.load();
    		listController = loader.getController();
    		listController.setHelper(this.parser.getHelper());
    		Stage stage = new Stage();
    		stage.setTitle("Ergebnisse");
    		stage.setScene(new Scene(root));
    		stage.showAndWait();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    @FXML
    void initialize() {
    	//counter
    	this.parser.getCounter().addListener((x,o,n) -> {
    		this.recordCounter.setText(String.valueOf(n.intValue()));
    	});
    	this.recordCounter.setText("0");
    	
    	//number of values to export
    	this.getAll.selectedProperty().addListener((x,o,n) -> {
    		if(n) {
    			getNumber.setDisable(true);
    		} else {
    			getNumber.setDisable(false);
    		}
    	});
    	this.getAll.setSelected(true);
    	
    	this.listView.getItems().addListener((ListChangeListener<File>) (x) -> {
    		Task<Long> worker = null;
			if(x.next()) {
				worker = parser.refreshCounter(x.getAddedSubList(),x.getRemoved());
		    	ProgressDialog dialog = new ProgressDialog(worker);
		    	dialog.setTitle("Files are analyzed files.");
		    	dialog.setContentText("The program is analyzing all selected files now.");
		    	exec.submit(worker);
		    	dialog.showAndWait();
			}
			if(worker != null) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Fully checked");
				alert.setContentText(String.format("Data has been counted in %d mills", worker.getValue()));
				alert.showAndWait();	
			}
		});
    	
    	int[] arr = {1,2,8,9,10,13,23,24};
    	List<Integer> list = new LinkedList<>();
    	for(int i : arr) {
    		list.add(i);
    	}
    	this.parser.getHelper().getUsedJSONAttributes().addAll(list);
    	
    	//load attribute names
    	try {
			this.parser.getHelper().loadAttributeNames(new FileInputStream(new File("resources/attributeNames.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
		
    	//load names of attributes in JSON file
    	try {
    		this.parser.getHelper().loadJSONNames(new FileInputStream(new File("resources/attributeJSONNames.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
    	
    	//load used attributes in JSON file
    	try {
    		this.parser.getHelper().loadUsedAttributes(new FileInputStream(new File("resources/usedAttributes.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
    }
    
    @Override
    public void finalize(){
    	exec.shutdownNow();
    }
}
