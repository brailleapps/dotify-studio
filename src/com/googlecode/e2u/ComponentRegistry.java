package com.googlecode.e2u;

import java.util.Hashtable;

import com.googlecode.ajui.AComponent;

public class ComponentRegistry {
	private Hashtable<String, AComponent> registry;
	public ComponentRegistry() {
		registry = new Hashtable<String, AComponent>();
	}
	
	public synchronized void register(AComponent c) {
		String id = c.getIdentifier();
		if (registry.containsKey(id)) {
			throw new IllegalArgumentException("Identifier already in use: " + id);
		}
		registry.put(id, c);
	}
	
	public synchronized AComponent getComponent(String id) {
		return registry.get(id);
	}
}
