package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Pair;

public class Parser {
	private final SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
	
	public Parser() {
		
	}

	public SimpleIntegerProperty getCounter() {
		return counter;
	}
	
	public List<Pair<Integer,Integer>> validate() {
		
		return null;
	}
	
	public void refreshCounter(List<? extends File> added, List<? extends File> removed) {
		long startTime = System.currentTimeMillis();
		added.parallelStream().forEach(x -> {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
				while(reader.readLine() != null) {
					counter.set(counter.get()+1);
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
					counter.set(counter.get()-1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {reader.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
		});
		long endTime = System.currentTimeMillis();
		System.out.println((endTime-startTime));
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Fully checked");
		alert.setContentText("Data has been counted");
		alert.showAndWait();
	}
}
