package com.googlecode.e2u;

public class MenuItem {
	private final String key;
	private final String name;
	private final MenuSystem menu;

	public MenuItem(String key, String name, MenuSystem menu) {
		this.key = key;
		this.name = name;
		this.menu = menu;
	}
	
	public MenuItem(String key, String name) {
		this(key, name, null);
	}
	
	public String getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasSubMenu() {
		return menu!=null;
	}

	public MenuSystem getSubMenu() {
		return menu;
	}

}