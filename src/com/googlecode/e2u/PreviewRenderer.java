package com.googlecode.e2u;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class PreviewRenderer {
	private boolean now = false;
	private final SwingWorker<File, Void> x;

	public PreviewRenderer(final URI uri, final int vol, final TransformerFactory factory, final PreviewController t, final Map<String, String> params) {
		 x = new SwingWorker<File, Void>() {

			@Override
			protected File doInBackground() throws Exception {
				long d = 200+(long)(Math.random()*100);
				while (true) {
					if (t.myTurn(vol) || now) {
						System.err.println("RUNNING VOL " + vol);
				        StreamSource xml1 = new StreamSource(uri.toURL().openStream());
				        File t1 = File.createTempFile("Preview", ".tmp");
				        t1.deleteOnExit();
				        Source xslt = new StreamSource(this.getClass().getResourceAsStream("resource-files/pef2xhtml.xsl"));
				        Transformer transformer = factory.newTransformer(xslt);
				        params.put("volume", ""+vol);
				        params.put("uriString", "view2.xml?book.xml");
				        for (String key : params.keySet()) {
				        	transformer.setParameter(key, params.get(key));
				        }
				        //transformer.setParameter("volume", ""+vol);
				        //transformer.setParameter("uriString", "view2.xml?book.xml");
				        transformer.transform(xml1, new StreamResult(t1));
				        //t.reportDone(vol);
						return t1;
					} else {
						Thread.sleep(d);
					}
				}
			}
			
		};
		new NewThreadExecutor().execute(x);
	}

	public boolean isDone() {
		return x.isDone();
	}

	public File getFile() {
		try {
			if (!isDone()) {
				now = true;
			}
			return x.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
