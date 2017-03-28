package com.googlecode.e2u;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class PreviewRenderer {
	private boolean now = false;
	private final SwingWorker<File, Void> x;
	private boolean abort = false;

	public PreviewRenderer(final URI uri, final int vol, final PreviewController t, final Map<String, String> params) {
		 x = new SwingWorker<File, Void>() {

			@Override
			protected File doInBackground() {
				long d = 200+(long)(Math.random()*100);
				while (!abort) {
					if (t.myTurn(vol) || now) {
						System.err.println("RUNNING VOL " + vol);
				        File t1 = null;
						try {
							t1 = File.createTempFile("Preview", ".tmp");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try (InputStream is = uri.toURL().openStream()){
							StreamSource xml1 = new StreamSource(is);

					        t1.deleteOnExit();
					        Source xslt = new StreamSource(this.getClass().getResourceAsStream("resource-files/pef2xhtml.xsl"));
					        TransformerFactory factory = TransformerFactory.newInstance();
					        factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
					        Transformer transformer = factory.newTransformer(xslt);
					        params.put("volume", ""+vol);
					        params.put("uriString", "view.html?book.xml");
					        for (String key : params.keySet()) {
					        	transformer.setParameter(key, params.get(key));
					        }
					        //transformer.setParameter("volume", ""+vol);
					        //transformer.setParameter("uriString", "view.html?book.xml");
					        transformer.transform(xml1, new StreamResult(t1));
					        //t.reportDone(vol);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (TransformerConfigurationException e) {
							e.printStackTrace();
						} catch (TransformerException e) {
							e.printStackTrace();
						}
						return t1;
					} else {
						try {
							Thread.sleep(d);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				return null;
			}
		};
		new NewThreadExecutor().execute(x);
	}

	public boolean isDone() {
		return x.isDone();
	}
	
	public void abort() {
		abort = true;
	}

	public File getFile() {
		try {
			if (!isDone()) {
				now = true;
			}
			return x.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            // TODO Auto-generated catch block
		return null;
	}
}
