package com.googlecode.e2u;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

import org.daisy.braille.pef.PEFBook;

public class BookScanner {
    private final static String REGEX = "[\\s\\.,:/-]";
    private final static boolean debug = false;

    private final ArrayList<PEFBook> books;
    private final Hashtable<String, Hashtable<File, PEFBook>> index;
    private final File path;

    private SwingWorker<Boolean, PEFBookFile> bookScanner;
    private static BookScanner instance = null;
    private AListener listener;
    private final PEFLibrary lib;
    private int doneCount = 0;

    private BookScanner(File dir) {
    	this.path = dir;
    	index = new Hashtable<String, Hashtable<File, PEFBook>>();
    	books = new ArrayList<PEFBook>();

    	lib = new PEFLibrary(dir);
    	
        bookScanner = new SwingWorker<Boolean, PEFBookFile>() {
        	Date d;

			@Override
			protected Boolean doInBackground() throws Exception {
				d = new Date();
				long diff = 0;
				for (File f : lib.getFileList()) {
					if (isCancelled()) {
						System.out.println("Cancelled!");
						return false;
					}
					try {
						Date t1 = new Date();
						publish(PEFBookFile.load(f));
						diff = System.currentTimeMillis()-t1.getTime();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Thread.sleep(diff);
				}
				return true;
			}
			
			protected void process(List<PEFBookFile> loaded) {
				PEFBook p;
				File f;
				for (PEFBookFile pbf : loaded) {
					p = pbf.getBook();
					f = pbf.getFile();
					//String identifier = p.getMetadata("identifier").iterator().next();
					books.add(p);
					if (debug) System.err.println("Adding book: " + p.getTitle());
					for (String key : p.getMetadataKeys()) {
						if ("application".equals(key)||"xml".equals(key)) {
							continue;
						}
						for (String val : p.getMetadata(key)) {
							for (String ind : val.toLowerCase().split(REGEX)) {
								if (ind!=null && ind.length()>0) {
									if ("identifier".equals(key)) {
										add(ind, f, p);
									} else if (ind.length()>3) {
										for (int i=3; i<=ind.length(); i++) {
											String indx = ind.substring(0, i);
											add(indx, f, p);
										}
									} else {
										add(ind, f, p);
									}
								}
							}
						}
					}
					doneCount++;
					notifyChange();
				}
			}
			
			private void add(String indx, File f, PEFBook p) {
				if (debug)  System.err.println("Adding index: " + indx);
				Hashtable<File, PEFBook> c = index.get(indx);
				if (c==null) {
					c = new Hashtable<File, PEFBook>();
					index.put(indx, c);
				}
				c.put(f, p);
			}
			
			protected void done() {
				try {
					System.out.println("Book Scanner " + (get()?"completed":"interrupted")+ ": " + (new Date().getTime() - d.getTime()));
					notifyChange();
				} catch (Exception e) {}
			}
       	
        };
        new NewThreadExecutor().execute(bookScanner);
    }
    
    public void notifyChange() {
    	listener.changeHappened(this);
    }
    
    public int getLibrarySize() {
    	return lib.getFileList().size();
    }
    
    public int getDoneCount() {
    	return doneCount;
    }
    
    public static BookScanner startScan(File dir) {
    	if (instance!=null) {
    		System.out.println("Canel...");
    		instance.bookScanner.cancel(true);
    		System.out.println(instance.bookScanner.getState());
    	}
    	instance = new BookScanner(dir);
    	return instance;
    }
    
    public void setEventListener(AListener listener) {
    	this.listener = listener;
    }
    
    public boolean isDone() {
    	return bookScanner.isDone();
    }
    
    public boolean cancel() {
    	return bookScanner.cancel(true);
    }
    
    public File getPath() {
    	return path;
    }
    
    public Hashtable<File, PEFBook> getBooks(String str) {
    	str = str.toLowerCase().replaceAll(REGEX, "");
    	if (debug)  System.err.println("Search for: " + str);
    	Hashtable<File, PEFBook> books = index.get(str);
    	
    	if (books==null) {
    		return new Hashtable<File, PEFBook>();
    	}
    	return books;
    }
    
    public Hashtable<File, PEFBook> containsAll(String str) {
    	String[] t = str.replaceAll(REGEX, " ").split("\\s");
    	ArrayList<String> ret = new ArrayList<String>();
    	for (String s : t) {
    		if (!"".equals(s) && s!=null) {
    			ret.add(s);
    		}
    	}
    	return containsAll(ret);
    }
    
    public Hashtable<File, PEFBook> containsAll(Iterable<String> strs) {
    	Hashtable<File, PEFBook> result = new Hashtable<File, PEFBook>();
    	//HashMap<File, Integer> toRemove = new HashMap<File, Integer>();
    	boolean first = true;
    	for (String s : strs) {
    		if (first) {
    			result.putAll(getBooks(s));
    			first = false;
    		} else {
    			Hashtable<File, PEFBook> r = getBooks(s);
    			Iterator<File> i = result.keySet().iterator();
    			while (i.hasNext()) {
    				File f = i.next();
    				if (r.get(f)==null) {
    					i.remove();
    				}
    			}
    		}
    	}
    	//for (File f : toRemove.keySet()) {
    	//	result.remove(f);
    	//}
    	return result;
    }
    
    public Hashtable<File, PEFBook> containsAll(String[] strs) {
    	return containsAll(Arrays.asList(strs));
    }
    
    public static void main(String[] args) throws InterruptedException, IOException {
    	//private final File dir = new File("D:\\books2");
    	BookScanner bs = BookScanner.startScan(new File("D:\\books2")); // start search
    	System.out.println("Scanning books. Wait a while...");
    	while (!bs.isDone()) {
    		Thread.sleep(300);
    	}
    	System.out.println("Get books...");
    	{
	    	ArrayList<String> st = new ArrayList<String>();
	    	st.add("rum");
	    	bs.printSearch(st);
    	}
    	{
	    	ArrayList<String> st = new ArrayList<String>();
	    	st.add("TPB");
    		bs.printSearch(st);
    	}
    	{
	    	ArrayList<String> st = new ArrayList<String>();
	    	st.add("polis");
	    	st.add("tåg");
    		bs.printSearch(st);
    	}
    	{
	    	ArrayList<String> st = new ArrayList<String>();
	    	st.add("P10367");
    		bs.printSearch(st);
    	}
    	System.out.println("Input search:");

    	LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
    	String line;
    	while (!"exit".equals((line = lnr.readLine().toLowerCase()))) {
    		bs.printSearch(line.split("\\s"));
    	}
    	System.out.println("End");
    }
    
    public void printSearch(Iterable<String> str) {
    	Hashtable<File, PEFBook> books = containsAll(str);
    	System.out.println("Search for " + str);
    	for (File key : books.keySet()) {
    		System.out.println("Book " + key);
    		Iterable<String> titles = books.get(key).getTitle();
    		if (titles != null) {
        		for (String val : titles) {
        			System.out.println(val);
        		}
    		}
    	}    	
    }
    
    public int getSize() {
    	return books.size();
    }
    
    public void printSearch(String[] strs) {
    	printSearch(Arrays.asList(strs));
    }

}
