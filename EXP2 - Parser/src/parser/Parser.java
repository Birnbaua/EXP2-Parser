package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import basics.ErrorCategory;
import basics.ErrorLog;
import dbPedia.DBPediaAirportLinker;
import inOut.Helper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Pair;

public class Parser {
	private final Helper helper = new Helper();
	private final SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
	
	public Parser() {
		
	}

	public SimpleIntegerProperty getCounter() {
		return counter;
	}
	
	public Task<List<ErrorLog>> validate(List<File> files) {
		return new Task<List<ErrorLog>>() {
			@Override
			protected List<ErrorLog> call() throws Exception {
				long startTime = System.currentTimeMillis();
				List<ErrorLog> errList = Collections.synchronizedList(new LinkedList<>());
				AtomicLong num = new AtomicLong(0);
				files.parallelStream().forEach(x -> {
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(x.getAbsolutePath()));
						String line = reader.readLine();
						while(line != null) {
							updateProgress(num.incrementAndGet(),counter.longValue());
							String[] arr = line.split(";");
							if(arr.length != 95) {
								errList.add(new ErrorLog(arr[7],"",ErrorCategory.WRONG_FORMAT));
							} else {
								//if you want to check the individual fields, do it here
								if(arr[0].length() != 4 || arr[1].length() != 4) {
									errList.add(new ErrorLog(arr[7],helper.getAttributeName(1),ErrorCategory.INVALID_VALUE));
								}
								// add attribute/record to the list 
								for(Integer nr : helper.getUsedJSONAttributes()) {
									if(arr[nr-1].length() == 0) {
										errList.add(new ErrorLog(arr[7],helper.getAttributeName(nr),ErrorCategory.NO_VALUE));
									}
								}
								/*
								 * space for further validation...
								 */
							}
							line = reader.readLine();
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
				System.out.println(endTime-startTime);
				return errList;
			}
    	};
	}
	
	public Task<Boolean> exportAsJson(List<File> list, File destination, long number){
		return new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				AtomicLong num = new AtomicLong(0);
				long startTime = System.currentTimeMillis();
				BufferedWriter writer = null;
				BufferedReader reader = null;
				TimeFormat format = new TimeFormat(1980,2079);
				DBPediaAirportLinker linker = new DBPediaAirportLinker();
				int lastJSONAttribute = helper.getUsedJSONAttributes().get(helper.getUsedJSONAttributes().size()-1);
				int fileCounter = 0;
				try {
					writer = new BufferedWriter(new FileWriter(destination,false));
					writer.write('[');
					for(File file : list) {
						reader = new BufferedReader(new FileReader(file));
						String line = reader.readLine();
						while(line != null) {
							String[] arr = line.split(";");
							
							//one object start
							writer.write('{');
							writer.newLine();
							for(Integer nr : helper.getUsedJSONAttributes()) {
								//if it is an airport
								if(nr == 1 || nr == 2) {
									writer.write(String.format("   \"%s\": \"%s\"", helper.getJSONName(nr), arr[nr-1]));
									writer.write(',');
									writer.newLine();
									if(nr == 1) {
										writer.write(String.format("   \"%s\": \"%s\"", "origin_uri", linker.getAirportURI(arr[nr-1])));
									}else {
										writer.write(String.format("   \"%s\": \"%s\"", "destination_uri", linker.getAirportURI(arr[nr-1])));
									}
								}else 
								//if it is time/date format
								if(nr == 9 || nr == 10 || nr == 23 || nr == 24) {
									if(nr == 9 || nr == 23) {
										writer.write(String.format("   \"%s\": \"%s\"", helper.getJSONName(nr), format.parseDate(arr[nr-1])));
									} else {
										writer.write(String.format("   \"%s\": \"%s\"", helper.getJSONName(nr), format.parseTime(arr[nr-1])));
									}
								} else {
									writer.write(String.format("   \"%s\": \"%s\"", helper.getJSONName(nr), arr[nr-1]));
								}
								if(nr != lastJSONAttribute) {
									writer.write(',');
								}
								writer.newLine();
							}
							writer.write('}');
							//one object end
							line = reader.readLine();
							
							updateProgress(num.incrementAndGet(),number);
							if(number == num.get()) {
								writer.write(']');
								long endTime = System.currentTimeMillis();
								System.out.println(endTime-startTime);
								return true;
							} else if(line != null) {
								writer.write(',');
								writer.newLine();
							}
						}
						fileCounter++;
						if(fileCounter != list.size()) {
							writer.write(',');
							writer.newLine();
						}
					}
					
					
					writer.write(']');
					long endTime = System.currentTimeMillis();
					System.out.println(endTime-startTime);
					
					
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				} finally {
					if(writer != null) {
						try {writer.close();} catch (IOException e) {e.printStackTrace();}
					}
					if(reader != null) {
						try {reader.close();} catch (IOException e) {e.printStackTrace();}
					}
				}
			}
			
		};
	}
	
	public Task<Long> refreshCounter(List<? extends File> added, List<? extends File> removed) {
		return new Task<Long>() {
			@Override
			protected Long call() throws Exception {
				AtomicLong updater = new AtomicLong();
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
					updateProgress(updater.incrementAndGet(), added.size() + removed.size());
					/*
					 * update counter, has to be synchronized because of parallelStream()
					 */
					synchronized(counter) {
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
					updateProgress(updater.incrementAndGet(), added.size() + removed.size());
					/*
					 * update counter, has to be synchronized because of parallelStream()
					 */
					synchronized(counter) {
						counter.set(counter.get()-num);
					}
				});
				return System.currentTimeMillis()-startTime;
			}
		};
		
	}
	
	

	public Helper getHelper() {
		return helper;
	}
}
