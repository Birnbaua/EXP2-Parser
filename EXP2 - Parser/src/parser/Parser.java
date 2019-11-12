package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Pair;

public class Parser {
	private final ObservableList<File> files;
	private final SimpleIntegerProperty counter = new SimpleIntegerProperty();
	
	public Parser(ObservableList<File> files) {
		this.files = files;
		this.files.addListener((ListChangeListener<File>) (x) -> {
			counter.set(0);
			refreshCounter(x.getAddedSubList(),x.getRemoved());
		});
	}

	public SimpleIntegerProperty getCounter() {
		return counter;
	}
	
	public List<Pair<Integer,Integer>> validate() {
		
		
		return null;
	}
	
	private void refreshCounter(List<? extends File> added, List<? extends File> removed) {
		added.parallelStream().forEach(x -> {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
				while(reader.readLine() != null) {
					counter.add(1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {reader.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
		});
		
		removed.parallelStream().forEach(x -> {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
				while(reader.readLine() != null) {
					counter.subtract(1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {reader.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
		});
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Fully checked");
		alert.setContentText("Data has been counted");
		alert.showAndWait();
	}
}
