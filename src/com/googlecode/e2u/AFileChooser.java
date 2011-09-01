package com.googlecode.e2u;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.AImage;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AListItem;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.AUnorderedList;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class AFileChooser extends AContainer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3842954642888453860L;
	private File currentDir = null;
	private AParagraph error;
	private MenuSystem menu;
	
	public AFileChooser(File startDir, MenuSystem menu) {
		if (startDir == null || !startDir.isDirectory()) {
			currentDir = new File(System.getProperty("user.home"));
			if (!currentDir.isDirectory()) {
				throw new IllegalArgumentException("user.home does not exist");
			}
		} else {
			currentDir = startDir;
		}

		setClass("group");
		this.menu = menu;
		rebuild();
	}
	
	private void rebuild() {
		clear();
		add(menu);

		Map<String, String> roots = new LinkedHashMap<String, String>();
		{
			List<File> c = getRoot(currentDir);
			for (File r : File.listRoots()) {
				if (c.get(0).equals(r)) {
					for (File r2 : c) {
						roots.put(r2.getAbsolutePath(), r2.getAbsolutePath());
					}
				} else {
					roots.put(r.getAbsolutePath(), r.getAbsolutePath());
				}
			}
		}
		error = new AParagraph();
		error.setClass("error");
		add(error);
		{
			AContainer div = new AContainer();
			div.setClass("path");
			ALink a = null;
			try {
				a = new ALink("index.html?method=find&path="+URLEncoder.encode(currentDir.getAbsolutePath(), MainPage.ENCODING));
				a.addAttribute("title", "Click to set library path");
				a.addAttribute("onclick", "return confirm('"+MessageFormat.format(Messages.getString(L10nKeys.SET_LIBRARY_PATH), currentDir.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"))+"')");
				AImage img = new AImage("images/home.png");
				img.addAttribute("border", "0");
				a.add(img);

			} catch (UnsupportedEncodingException e) { }
			SelectComponent sc = new SelectComponent(roots, a, "dir", "Path", false, "choose");
			sc.setSelected(currentDir.getAbsolutePath());
			div.add(sc);
			add(div);
		}
	
		ArrayList<File> dirs = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();
		// optimize for speed by not using listFiles(FileFilter)
		{
			File c;
			for (String name : currentDir.list()) {
				c = new File(currentDir, name);
				if (c.isDirectory()) {
					dirs.add(c);
				} else if (name.endsWith(".pef")) {
					files.add(c);
				}
			}
		}
		{
			AContainer div = new AContainer();
			div.setClass("dirSelect");
			AUnorderedList ul = new AUnorderedList();
			ul.setClass("fileChooser");
			if (currentDir.getParentFile()!=null) {
				try {
					AListItem li = new AListItem();
					li.setClass("dir");
					ALink a = new ALink("index.html?method=choose&dir=" + URLEncoder.encode(currentDir.getParentFile().getAbsolutePath(), MainPage.ENCODING));
					AImage img = new AImage("images/folder.gif");
					img.addAttribute("border", "0");
					a.add(img);
					a.add(new ALabel(".."));
					li.add(a);
					ul.add(li);
				} catch (UnsupportedEncodingException e) { }
			}
			for (File f : dirs) {
				try {
					String encURL = URLEncoder.encode(f.getAbsolutePath(), MainPage.ENCODING);
					AListItem li = new AListItem();
					li.setClass("dir");
					ALink a = new ALink("index.html?method=choose&dir=" + encURL);
					AImage img = new AImage("images/folder.gif");
					img.addAttribute("border", "0");
					a.add(img);
					a.add(new ALabel(f.getName()));
					li.add(a);
					ul.add(li);
				} catch (UnsupportedEncodingException e) { }
			}
			div.add(ul);
			add(div);
		}

		{
			AContainer div = new AContainer();
			div.setClass("fileSelect");
			if (files.size()>0) {
				AUnorderedList ul = new AUnorderedList();
				ul.setClass("fileChooser");
				for (File f : files) {
					try {
						String encURL = URLEncoder.encode(f.getAbsolutePath(), MainPage.ENCODING);
						AListItem li = new AListItem();
						li.setClass("file");
						ALink a = new ALink("index.html?open=" + encURL);
						a.add(new ALabel(f.getName()));
						li.add(a);
						ul.add(li);
					} catch (UnsupportedEncodingException e) { }
				}
				div.add(ul);
			}
			
			add(div);
		}

	}
	
	private List<File> getRoot(File f) {
		ArrayList<File> ret = new ArrayList<File>();
		while (f!=null) {
			ret.add(0, f);
			f = f.getParentFile();
		}
		return ret;
	}

	@Override
	public XHTMLTagger getHTML(Context context) {
		String startDir = context.getArgs().get("dir");
		error.clear();
		if (startDir!=null) {
			try {
				File f = new File(URLDecoder.decode(startDir, MainPage.ENCODING));
				if (!f.equals(currentDir)) {
					if (f.isDirectory()) {
						currentDir = f;
						rebuild();
					} else {
						
						error.add(new ALabel(MessageFormat.format(Messages.getString(L10nKeys.COULD_NOT_SET_DIRECTORY), startDir)));
					}
				}
			} catch (UnsupportedEncodingException e) { }
		}
		return super.getHTML(context);
	}
	
}
