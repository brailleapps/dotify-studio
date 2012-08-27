package com.googlecode.e2u;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Set;

import org.daisy.braille.pef.PEFBook;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AListItem;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.AUnorderedList;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.Settings.Keys;

public class FindView extends AContainer implements AListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1625359388549494302L;
	private BookScanner bs;
	private AContainer findResults;
	private ALabel libraryPathLabel;
	private String prevFind = "";
	private AParagraph scanningInProgress;
	private ALabel scanningInProgressLabel;
	private Settings settings;
	private AParagraph error;

	public FindView(Settings settings, MenuSystem menu, ComponentRegistry registry) {
		setClass("group");
		this.settings = settings;
		add(menu);
		error = new AParagraph();
		error.setClass("error");
		add(error);
/*		

		*/
		
		AParagraph libraryPathP = new AParagraph();
		libraryPathLabel = new ALabel("");
		libraryPathP.add(libraryPathLabel);
		add(libraryPathP);
		
    	scanningInProgress = new AParagraph();
    	scanningInProgress.setIdentifier("scanning-in-progress");
    	registry.register(scanningInProgress);
    	scanningInProgressLabel = new ALabel("Scanning in progress...");
    	add(scanningInProgress);
    	
    	{
    		InputSelectComponent in = new InputSelectComponent("this", "Find", "find");
    		add(in);
    	}
    	
		//tagger.startForm(context.getTarget());
		/*
		tagger.start("p");
		tagger.start("span").attr("class", "settingName").text("Find").end().text(" ");
		tagger.start("input").attr("type", "hidden").attr("name", "method").attr("value", "find").end();
		tagger.start("input").attr("type", "text").attr("name", "this").attr("value", find).end();
		tagger.end();
		tagger.end();*/
		
		findResults = new AContainer();
		findResults.setClass("findResult");
		findResults.setIdentifier("findResults");
		registry.register(findResults);
		add(findResults);
		startScanning();
	}
	
    private void startScanning() {
    	if (bs!=null) {
    		bs.cancel();
    	}
    	File libPath = settings.getLibraryPath();
   		bs = BookScanner.startScan(libPath);
   		libraryPathLabel.setText(libPath.getAbsolutePath());
   		bs.setEventListener(this);
    }
	/*
	public void setLibraryPath() {
		libraryPathP.add(new ALabel("Library path: " + bs.getPath().getAbsolutePath()));
	}
*/
	@Override
	public XHTMLTagger getHTML(Context context) {
		error.clear();
		String find = context.getArgs().get("this");

		if (context.getArgs().get("path")!=null) { 
			String cPath = settings.getString(Keys.libraryPath);
			String path = settings.getSetPref(Keys.libraryPath, context.getArgs().get("path"), System.getProperty("user.home"));
	
			if (!new File(path).exists() || path.equals("")) {
				error.add(new ALabel("Path does not exist"));
				settings.resetKey(Keys.libraryPath);
			} else {
				if (!path.equals(cPath)) {
					startScanning();
					find = "";
				}
			}
		}
		scanningInProgress.clear();
		if (!bs.isDone()) {
			scanningInProgress.add(scanningInProgressLabel);
		}

		if (find==null) {
			find = prevFind;
		} else {
			try {
				find = URLDecoder.decode(find, MainPage.ENCODING);
				prevFind = find;
			} catch (UnsupportedEncodingException e) {}
		}
		findResults.clear();
		if (!"".equals(find)) {
			Hashtable<File, PEFBook> res = bs.containsAll(find);
			Set<File> set = res.keySet();
			{
				AParagraph p = new AParagraph();
				p.add(new ALabel("Results: " + set.size() + "(" + bs.getSize() + ")"));
				findResults.add(p);
			}
			if (set.size()>0) {
				AUnorderedList ul = new AUnorderedList();
				for (File key : set) {
					PEFBook b = res.get(key);
					if (b!=null) {
						try {
							String encURL = URLEncoder.encode(key.getAbsolutePath(), MainPage.ENCODING);
							AListItem li = new AListItem();
							li.setClass("file");
							ALink a = new ALink("index.html?open="+encURL);
							ALabel label;
							Iterable<String> title = b.getTitle(); 
							Iterable<String> authors = b.getAuthors();
							if (title==null && authors==null) {
								label = new ALabel(key.getName());
							} else {
								label = new ALabel(
										(title==null?"Untitled":title.iterator().next()) + " av " + 
										(authors==null?"Unknown":authors.iterator().next()));
							}
							a.add(label);
							li.add(a);
							ul.add(li);
						} catch (UnsupportedEncodingException e) { }
					}
				}
				findResults.add(ul);
			}
		}
		
		return super.getHTML(context);
	}
	
	public void close() {
		bs.cancel();
	}

	@Override
	public void changeHappened(Object o) {
		if (bs.isDone()) {
			scanningInProgressLabel.setText("");
		} else {
			scanningInProgressLabel.setText("Scanning in progress... " + bs.getDoneCount());
		}
		scanningInProgress.update();
		//findResults.update();
	}
	
}
