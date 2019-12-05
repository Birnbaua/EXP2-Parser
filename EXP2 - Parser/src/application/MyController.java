package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.controlsfx.dialog.ProgressDialog;


import application.attributes.ListController;
import application.validation.ValidationController;
import basics.ErrorLog;
import dbPedia.DBPediaAirportLinker;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
	@FXML private Button editAttributes;
	@FXML private Button refreshURIButton;
	@FXML private CheckBox withURIs;
	@FXML private CheckBox withDepartureDelay;
	
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
		fileChooser.setTitle("Import EXP2 files");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".exp2", "*.exp2"));
		List<File> files = fileChooser.showOpenMultipleDialog(stage);
		if(files != null) {
			this.listView.getItems().addAll(files);
			this.directory = files.get(0).getParentFile();
		}
    }
    
    @FXML
    void onValidate(ActionEvent event) {
    	Task<List<ErrorLog>> worker = parser.validate(this.listView.getItems());
    	ProgressDialog dialog = new ProgressDialog(worker);
    	dialog.setTitle("Validating files.");
    	dialog.setContentText("The program is validating all listed files now.");
    	exec.submit(worker);
    	dialog.showAndWait();
    	
    	Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Show List");
    	alert.setHeaderText("Do you want to show all errors?");
    	alert.getButtonTypes().clear();
    	ButtonType yes = new ButtonType("Yes");
    	ButtonType no = new ButtonType("No");
    	alert.getButtonTypes().addAll(yes,no);
    	
    	if(alert.showAndWait().get() == yes) {
        	ValidationController validationController = null;
        	Parent root;
        	FXMLLoader loader;
        	try {
        		loader = new FXMLLoader(ValidationController.class.getResource("Validation.fxml"));
        		root = loader.load();
        		validationController = loader.getController();
        		validationController.addList(worker.getValue());
        		Stage stage = new Stage();
        		stage.setTitle("Results");
        		stage.setScene(new Scene(root));
        		stage.showAndWait();
        	} catch(IOException e) {
        		e.printStackTrace();
        	}
    	}
    }
    
    @FXML
    void onExport(ActionEvent event) {
    	if(this.parser.getHelper().getAirportURIs().size() == 0) {
    		Alert alert = new Alert(AlertType.CONFIRMATION);
        	alert.setTitle("Show List");
        	alert.setHeaderText("You have to reload the URIs from DBPedia, do you want to do that now?");
        	alert.getButtonTypes().clear();
        	ButtonType yes = new ButtonType("Yes");
        	ButtonType no = new ButtonType("No");
        	alert.getButtonTypes().addAll(yes,no);
        	if(alert.showAndWait().get() == no) {
        		return;
        	} else {
        		DBPediaAirportLinker linker = new DBPediaAirportLinker();
        		for(Entry<String, String> entry : linker.getAirports().entrySet()) {
        			parser.getHelper().getAirportURIs().put(entry.getKey(), entry.getValue());
        		}
        		try {
            		this.parser.getHelper().saveAirportURIs(new FileOutputStream(new File("resources/airportURIs.properties").getAbsolutePath()));
        		} catch (FileNotFoundException e) {e.printStackTrace();
        		} catch (IOException e) {e.printStackTrace();}
        	}
    	}
    	Stage stage = new Stage();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if(directory != null) {
			directoryChooser.setInitialDirectory(directory);
		}
		directoryChooser.setTitle("Choose Export Directory");
		File file = directoryChooser.showDialog(stage);
		parser.setWithAirportURIs(withURIs.isSelected());
		parser.setWithDepartureDelay(withDepartureDelay.isSelected());
		Task<Boolean> worker = parser.exportAsJson(this.listView.getItems(), new File(file.getAbsoluteFile() + "//converted.json"), 
				getAll.isSelected() ? this.parser.getCounter().longValue() : Long.parseLong(this.getNumber.getText()));
    	ProgressDialog dialog = new ProgressDialog(worker);
    	dialog.setTitle("Exporting files.");
    	dialog.setContentText("The program is exporting all listed files now.");
    	exec.submit(worker);
    	dialog.showAndWait();
    	System.out.print(worker.getValue());
    }
    
    @FXML
    void onEdit() {
    	ListController listController = null;
    	Parent root;
    	FXMLLoader loader;
    	try {
    		loader = new FXMLLoader(ListController.class.getResource("Attributes.fxml"));
    		root = loader.load();
    		listController = loader.getController();
    		listController.setHelper(this.parser.getHelper());
    		Stage stage = new Stage();
    		stage.setTitle("Ergebnisse");
    		stage.setScene(new Scene(root));
    		
    		//ask if changes should be saved
    		ListController contr = listController;
    		stage.setOnCloseRequest(event -> {
    			if(contr.hasChanged()) {
    				Alert alert = new Alert(AlertType.CONFIRMATION);
                	alert.setTitle("Save");
                	alert.setHeaderText("Do you want to save changes?");
                	alert.getButtonTypes().clear();
                	ButtonType yes = new ButtonType("Yes");
                	ButtonType no = new ButtonType("No");
                	alert.getButtonTypes().addAll(yes,no);
                	if(alert.showAndWait().get() == yes) {
                		contr.onSave();
                	} else {
                		try {
    						this.parser.getHelper().loadAttributeNames(new FileInputStream(new File("resources/attributeNames.properties").getAbsolutePath()));
    			    		this.parser.getHelper().loadUsedAttributes(new FileInputStream(new File("resources/usedAttributes.properties").getAbsolutePath()));
    			    		this.parser.getHelper().loadJSONNames(new FileInputStream(new File("resources/attributeJSONNames.properties").getAbsolutePath()));
    					} catch (FileNotFoundException e) {e.printStackTrace();
    					} catch (IOException e) {e.printStackTrace();}
                	}
    			}
    		});
    		
    		stage.showAndWait();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    @FXML 
    void onRefresh() {
    	Task<DBPediaAirportLinker> worker = new Task<DBPediaAirportLinker>() {
			@Override
			protected DBPediaAirportLinker call() throws Exception {return new DBPediaAirportLinker();}
    	};
    	ProgressDialog dialog = new ProgressDialog(worker);
    	dialog.setTitle("Refreshing URIs.");
    	dialog.setContentText("The program is refreshing all URIs from DBPedia now.");
    	exec.submit(worker);
    	dialog.showAndWait();
    	if(worker.getValue().getAirports() == null || worker.getValue().getAirports().size() == 0) {
    		return;
    	}
    	parser.getHelper().getAirportURIs().clear();
		for(Entry<String, String> entry : worker.getValue().getAirports().entrySet()) {
			parser.getHelper().getAirportURIs().put(entry.getKey(), entry.getValue());
		}
		try {
    		this.parser.getHelper().saveAirportURIs(new FileOutputStream(new File("resources/airportURIs.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
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
    	
    	ContextMenu contextMenu = new ContextMenu();
    	MenuItem remove = new MenuItem("Remove Selected");
    	contextMenu.getItems().add(remove);
    	remove.setOnAction(event -> {
    		this.listView.getItems().removeAll(this.listView.getSelectionModel().getSelectedItems());
    		
    		/*
    		 * counter subtraction 
    		 */
    	});
    	
    	this.listView.setOnMouseClicked(value -> {
    		if(value.getButton().equals(MouseButton.PRIMARY)) {
    			contextMenu.hide();
    		}
    	});
    	
    	this.listView.setOnContextMenuRequested(event -> {
    		contextMenu.show(listView,event.getScreenX(),event.getScreenY());
    	});
    	
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
    	
    	//load airportURIs file
    	try {
    		this.parser.getHelper().loadAirportURIs(new FileInputStream(new File("resources/airportURIs.properties").getAbsolutePath()));
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
    }
    
    @Override
    public void finalize(){
    	exec.shutdownNow();
    }
}
