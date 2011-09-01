package com.googlecode.e2u;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.daisy.braille.pef.PEFBook;
import org.xml.sax.SAXException;

public class PEFBookFile {
	private final static File dir = new File(System.getProperty("java.io.tmpdir"));
	
	private final PEFBook book;
	private final File f;
	private PEFBookFile(PEFBook book, File f) {
		this.book = book;
		this.f = f;
	}
	
	public PEFBook getBook() {
		return book;
	}
	
	public File getFile() {
		return f;
	}
	
	public static PEFBookFile load(File f) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		File serial = new File(dir, f.getName()+"-"+f.hashCode()+".meta");
		PEFBook book;
		if (serial.exists() && serial.lastModified()>f.lastModified()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serial));
			try {
				book = (PEFBook)ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				book = PEFBook.load(f.toURI());
				if (!serial.delete()) {
					serial.deleteOnExit();
				}
			} finally {
				ois.close();
			}
		} else {
			book = PEFBook.load(f.toURI());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serial));
			try {
				oos.writeObject(book);
			} finally {
				oos.close();
			}
		}
		return new PEFBookFile(book, f);
	}

}
