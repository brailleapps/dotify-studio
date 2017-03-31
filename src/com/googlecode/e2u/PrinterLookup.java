package com.googlecode.e2u;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.SwingWorker;

public class PrinterLookup {
	private static final Logger logger = Logger.getLogger(PrinterLookup.class.getCanonicalName());
	private static PrinterLookup instance = null;
    private SwingWorker<PrintService[], Void> printerLookup;
    private PrintService[] printers = null;

    private PrinterLookup() {
        printerLookup = new SwingWorker<PrintService[], Void>() {
        	Date d;

			@Override
			protected PrintService[] doInBackground() throws Exception {
				d = new Date();
				return PrintServiceLookup.lookupPrintServices(null, null);
			}
			
                @Override
	       protected void done() {
	    	   logger.info("Printer lookup: " + (new Date().getTime() - d.getTime()));
	       }
       	
        };
        new NewThreadExecutor().execute(printerLookup);
    }
    
    public static synchronized PrinterLookup getInstance() {
    	if (instance==null) {
    		instance = new PrinterLookup();
    	}
    	return instance;
    }
    
    public boolean isDone() {
    	return printerLookup.isDone();
    }
    
    public synchronized PrintService[] getPrinters() {
    	if (printers==null) {
    		try {
				printers = printerLookup.get();
			} catch (InterruptedException | ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	return printers;
    }

}
