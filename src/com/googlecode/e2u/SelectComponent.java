package com.googlecode.e2u;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.factory.FactoryProperties;

import com.googlecode.ajui.ABlockComponent;
import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AOption;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.ASelect;
import com.googlecode.ajui.ASpan;
import com.googlecode.ajui.AbstractComponent;

public class SelectComponent extends AbstractComponent<ABlockComponent> implements ABlockComponent, Selectable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6590046424943180089L;
	
	private final ASelect select;
	private final AParagraph desc;
	private final HashMap<String, String> descriptions;

	public SelectComponent(Map<String, String> fo, String selectName, String displayName, boolean emptyOption, String target) {
		this(fo, null, selectName, displayName, emptyOption, target);
	}
	
	public SelectComponent(Map<String, String> fo, ALink link, String selectName, String displayName, boolean emptyOption, String target) {
		addAttribute("action", "/index.html");
		addAttribute("method", "get");
		AContainer div = new AContainer();
		{
			AParagraph p = new AParagraph();
			ASpan s = new ASpan();
			s.setClass("settingName");
			s.add(new ALabel(displayName));
			if (link!=null) {
				s.add(link);
			}
			p.add(s);
			select = new ASelect();
			select.addAttribute("name", selectName);
			select.addAttribute("onchange", "submit();");
			if (emptyOption) {
				select.add(new AOption("", ""));
			}
			for (String o : fo.keySet()) {
				select.add(new AOption(fo.get(o), o.toString()));
	    	}
			p.add(select);
			div.add(p);
		}
		{
			desc = new AParagraph();
			desc.setClass("desc");
			div.add(desc);
		}
		add(div);
		{
			AParagraph p = new AParagraph();
			AInput input = new AInput();
			input.addAttribute("type", "hidden");
			input.addAttribute("name", "method");
			input.addAttribute("value", target);
			p.add(input);
			add(p);
		}
		descriptions = new HashMap<String, String>();
	}

	public SelectComponent(Collection<? extends FactoryProperties> fo, String selectName, HashMap<String, String> nn, String displayName, boolean emptyOption, String target) {
		addAttribute("action", "/index.html");
		addAttribute("method", "get");
		List<FactoryProperties> list = new ArrayList<FactoryProperties>();
    	list.addAll(fo);
    	Collections.sort(list, new Comparator<FactoryProperties>() {

			@Override
			public int compare(FactoryProperties o1, FactoryProperties o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
    	AContainer div = new AContainer();
    	{
			AParagraph p = new AParagraph();
			ASpan s = new ASpan();
			s.setClass("settingName");
			s.add(new ALabel(displayName));
			p.add(s);
			select = new ASelect();
			select.addAttribute("name", selectName);
			select.addAttribute("onchange", "submit();");
			if (emptyOption) {
				select.add(new AOption("", ""));
			}
			descriptions = new HashMap<String, String>();
	    	for (FactoryProperties o : list) {
		    	String disp;
		    	if (nn!=null && nn.containsKey(o.getIdentifier())) {
		    		disp = nn.get(o.getIdentifier());
		    	} else {
		    		disp = o.getDisplayName();
		    	}
		    	descriptions.put(o.getIdentifier(), o.getDescription());
		    	select.add(new AOption(disp, o.getIdentifier()));
	    	}
			p.add(select);
			div.add(p);
    	}
    	{
			desc = new AParagraph();
			desc.setClass("desc");
			div.add(desc);
    	}
    	add(div);
		{
			AParagraph p = new AParagraph();
			AInput input = new AInput();
			input.addAttribute("type", "hidden");
			input.addAttribute("name", "method");
			input.addAttribute("value", target);
			p.add(input);
			add(p);
		}
	}
	
	public void setSelected(String value) {
		String d = descriptions.get(value);
		desc.clear();
    	if (d!=null && !"".equals(d)) {
    		desc.add(new ALabel(d));
    	}
		select.setSelected(value);
	}

	@Override
	protected String getTagName() {
		return "form";
	}

}
