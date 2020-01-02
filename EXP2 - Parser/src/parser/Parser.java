package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import basics.ErrorCategory;
import basics.ErrorLog;
import inOut.Helper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class Parser {
	private final Helper helper = new Helper();
	private final SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
	private boolean isWithAirportURIs = false;
	private boolean withDepartureDelay = false;
	private boolean isWithErrors = false;
	private Set<Integer> idSet = new TreeSet<>();
	
	public Parser(boolean isWithAirportURIs) {
		this.isWithAirportURIs = isWithAirportURIs;
	}
	
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
							isValid(arr,errList);
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
	
	public Task<AtomicLong> exportAsJson(List<File> list, File destination, long number){
		return new Task<AtomicLong>() {
			@Override
			protected AtomicLong call() throws Exception {
				idSet.clear();
				AtomicLong num = new AtomicLong(0);
				AtomicLong numberOfExportedLines = new AtomicLong(0);
				List<ErrorLog> errList = new LinkedList<>();
				long startTime = System.currentTimeMillis();
				BufferedWriter writer = null;
				BufferedReader reader = null;
				TimeFormat format = new TimeFormat(1980,2079);
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
							boolean isWritten = false;
							if((isValid(arr,errList) || isWithErrors) && isAlreadyInJson(Integer.parseInt(arr[7])) == false) {
								isWritten = true;
								//one object start
								writer.write('{');
								writer.newLine();
								numberOfExportedLines.incrementAndGet();
								for(Integer nr : helper.getUsedJSONAttributes()) {
									//if it is an airport
									if(nr == 1 || nr == 2) {
										writer.write(String.format("   \"%s\": \"%s\"", helper.getJSONName(nr), arr[nr-1]));
										if(isWithAirportURIs) {
											writer.write(',');
											writer.newLine();
											String airportURI = helper.getAirportURI(arr[nr-1]);
											if(nr == 1) {
												writer.write(String.format("   \"%s\": \"%s\"", "origin_uri", airportURI));
											}else {
												writer.write(String.format("   \"%s\": \"%s\"", "destination_uri", airportURI));
											}
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
										if(nr == 8) {
											writer.write(String.format("   \"%s\": %s", helper.getJSONName(nr), arr[nr-1]));
										} else {
											writer.write(String.format("   \"%s\": \"%s\"", helper.getJSONName(nr), arr[nr-1]));
										}
									}
									if(nr != lastJSONAttribute || withDepartureDelay) {
										writer.write(',');
									}
									writer.newLine();
								}
								if(withDepartureDelay) {
									writer.write(String.format("   \"departureDelay\": %d", Delay.getDelay(arr[22], arr[23], arr[8], arr[9])));
									writer.newLine();
								}
								writer.write('}');
								//one object end
							}
							line = reader.readLine();
							
							updateProgress(num.incrementAndGet(),number);
							if(number == numberOfExportedLines.get()) {
								writer.write(']');
								long endTime = System.currentTimeMillis();
								System.out.println(endTime-startTime);
								return numberOfExportedLines;
							} else if(line != null && isWritten) {
								writer.write(',');
								writer.newLine();
							}
							isWritten = false;
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
					
					
					return numberOfExportedLines;
				} catch (IOException e) {
					e.printStackTrace();
					return numberOfExportedLines;
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
	
	private boolean isValid(String[] arr, List<ErrorLog> errList) {
		if(arr.length != 95) {
			errList.add(new ErrorLog(arr[7],"",ErrorCategory.WRONG_FORMAT));
			return false;
		} else {
			for(Integer nr : helper.getUsedJSONAttributes()) {
				if(arr[nr-1].length() == 0) {
					errList.add(new ErrorLog(arr[7],helper.getAttributeName(nr),ErrorCategory.NO_VALUE));
					return false;
				}
			}
			
			//airports
			if(arr[0].length() != 4 || arr[1].length() != 4) {
				if(arr[0].length() != 4) {
					errList.add(new ErrorLog(arr[7],helper.getAttributeName(1),ErrorCategory.WRONG_FORMAT));
				} else {
					errList.add(new ErrorLog(arr[7],helper.getAttributeName(2),ErrorCategory.WRONG_FORMAT));
				}
				return false;
			} else if(isWithAirportURIs && (helper.getAirportURI(arr[0]) == null || helper.getAirportURI(arr[1]) == null)) {
				if(helper.getAirportURI(arr[0]) == null) {
					ErrorLog log = new ErrorLog(arr[7],helper.getAttributeName(1),ErrorCategory.NO_URI);
					errList.add(log);
				} else {
					errList.add(new ErrorLog(arr[7],helper.getAttributeName(2),ErrorCategory.NO_URI));
				}
				return false;
			}
			
			//validate format of departure dates and time
			if(arr[8].length() != 6 || arr[9].length() != 4 || arr[22].length() != 6 || arr[23].length() != 4) {
				return false;
			} else {
				try {
					int[] nr = {8,9,22,23};
					for(int n : nr) {
						Integer.parseInt(arr[n]);
					}
				} catch(NumberFormatException e) {
					errList.add(new ErrorLog(arr[7],helper.getAttributeName(1),ErrorCategory.WRONG_FORMAT));
					return false;
				}
			}
		}
		return true;
	}

	public Helper getHelper() {
		return helper;
	}
	
	public boolean isWithAirportURIs() {
		return isWithAirportURIs;
	}

	public void setWithAirportURIs(boolean isWithAirportURIs) {
		this.isWithAirportURIs = isWithAirportURIs;
	}

	public boolean isWithDepartureDelay() {
		return withDepartureDelay;
	}

	public void setWithDepartureDelay(boolean withDeparture) {
		this.withDepartureDelay = withDeparture;
	}
	
	public boolean isWithErrors() {
		return isWithErrors;
	}

	public void setIsWithErrors(boolean withErrors) {
		this.isWithErrors = withErrors;
	}
	
	private boolean isAlreadyInJson(int id) {
		if(idSet.contains(id)) {
			return true;
		} else {
			idSet.add(id);
			return false;
		}
	}
}
