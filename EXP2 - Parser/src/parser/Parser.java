package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;

public class Parser {
	private final SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
	
	public Parser() {
		
	}

	public SimpleIntegerProperty getCounter() {
		return counter;
	}
	
	public List<Pair<String,Integer>> validate(List<File> files, ProgressBar pb) {
		long startTime = System.currentTimeMillis();
		List<Pair<String,Integer>> flawList = new LinkedList<>();
		SimpleDoubleProperty progress = new SimpleDoubleProperty();
		AtomicInteger num = new AtomicInteger(0);
		pb.progressProperty().add(progress);
		files.parallelStream().forEach(x -> {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
				String line = reader.readLine();
				int i = 0;
				while(line != null) {
					i++;
					String[] arr = line.split(";");
					if(arr.length != 95) {
						flawList.add(new Pair<String,Integer>(x.getName(),i));
					} else {
						//if you want to check the individual fields, do it here
						if(arr[0].length() != 4 || arr[1].length() != 4) {
							flawList.add(new Pair<String,Integer>(x.getName(),i));
						}
					}
					line = reader.readLine();
					progress.set((double)num.incrementAndGet()/counter.get());
				}
				progress.set(1);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {reader.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
		});
		long endTime = System.currentTimeMillis();
		System.out.println(endTime-startTime);
		return flawList;
	}
	
	public void refreshCounter(List<? extends File> added, List<? extends File> removed) {
		long startTime = System.currentTimeMillis();
		added.parallelStream().forEach(x -> {
			BufferedReader reader = null;
			int num = 0;
			try {
				reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
				while(reader.readLine() != null) {
					num++;;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {reader.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
			
			/*
			 * update counter, has to be synchronized because of parallelStream()
			 */
			synchronized(this.counter) {
				counter.set(counter.get()+num);
			}
		});
		
		removed.parallelStream().forEach(x -> {
			BufferedReader reader = null;
			int num = 0;
			try {
				reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
				while(reader.readLine() != null) {
					num++;;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {reader.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
			/*
			 * update counter, has to be synchronized because of parallelStream()
			 */
			synchronized(this.counter) {
				counter.set(counter.get()-num);
			}
		});
		long endTime = System.currentTimeMillis();
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Fully checked");
		alert.setContentText(String.format("Data has been counted in %d mills", endTime-startTime));
		alert.showAndWait();
	}
}
