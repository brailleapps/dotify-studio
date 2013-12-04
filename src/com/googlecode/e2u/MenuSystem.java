package com.googlecode.e2u;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.googlecode.ajui.ABlockComponent;
import com.googlecode.ajui.AComponent;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;

public class MenuSystem implements ABlockComponent {
	private final String getKey;
	//private final String name;
	private final MenuSystem parent;
	private final List<MenuItem> list;
	private String divider;
	private boolean clickActive;
	

	public MenuSystem(String getKey) {
		this(getKey, "", null);
	}
	
	public MenuSystem(String getKey, MenuSystem parent) {
		this(getKey, "", parent);
	}

	public MenuSystem(String getKey, String name, MenuSystem parent) {
		this.getKey = getKey;
		//this.name = name;
		this.parent = parent;
		this.list = new ArrayList<MenuItem>();
		this.clickActive = true;
	}
	
	public MenuSystem setDivider(String divider) {
		this.divider = divider;
		return this;
	}
	
	public MenuSystem setClickActive(boolean value) {
		this.clickActive = value;
		return this;
	}

	public MenuSystem addMenuItem(String key, String name) {
		addMenuItem(new MenuItem(key, name));
		return this;
	}
	
	private void registerItem(String key, String value, int level) {
		if (parent!=null) {
			parent.registerItem(key, value, level+1);
		}
		//map.add(key=value, )
	}
	
	public MenuSystem addMenuItem(MenuItem item) {
		list.add(item);
		if (parent!=null) {
			parent.registerItem(getKey, item.getKey(), 2);
		}
		return this;
	}
	
	public boolean contains(String key) {
		for (MenuItem m : list) {
			if (m.getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	public XHTMLTagger getHTML(Context context) {
		XHTMLTagger tagger = new XHTMLTagger();
		tagger.start("p");
		boolean first = true;
		String current = context.getArgs().get(getKey);
		if (current==null) {
			current = list.get(0).getKey();
		}
		for (MenuItem m : list) {
			if (first) {
				first = false;
			} else {
				if (divider!=null && !divider.equals("")) {
					tagger.text(divider);
					
				}
			}
			tagger.start("span").attr("class", "menuItem" + (current.equals(m.getKey())?" active":""));
			if (clickActive || !current.equals(m.getKey())) { 
				tagger.start("a").attr("href", MainPage.TARGET+"?"+getKey+"="+m.getKey());
			}
			tagger.text(m.getName());
			if (clickActive || !current.equals(m.getKey())) {
				tagger.end();
			}
			tagger.end();
		}
		tagger.end();
		return tagger;
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends AComponent> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasUpdates(Date since) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasIdentifer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

}
