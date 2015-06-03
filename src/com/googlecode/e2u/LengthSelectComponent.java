package com.googlecode.e2u;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.ajui.ABlockComponent;
import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.AOption;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.ASelect;
import com.googlecode.ajui.ASpan;
import com.googlecode.ajui.AbstractComponent;

public class LengthSelectComponent extends AbstractComponent<ABlockComponent> implements ABlockComponent, Selectable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6590046424943180089L;
	
	private final ASelect select;
	private final AParagraph desc;
	private final AInput in;
	private final HashMap<String, String> descriptions;

	public LengthSelectComponent(Map<String, String> fo, String selectName, String inputName, String displayName, boolean emptyOption, String target) {
		addAttribute("action", "/index.html");
		addAttribute("method", "get");
		AContainer div = new AContainer();
		{
			AParagraph p = new AParagraph();
			ASpan s = new ASpan();
			s.setClass("settingName");
			s.add(new ALabel(displayName));
			p.add(s);
			ASpan s2 = new ASpan();
			s2.setClass("length");
			in = new AInput();
			in.addAttribute("type", "text");
			in.addAttribute("name", inputName);
			s2.add(in);
			select = new ASelect();
			select.setClass("length");
			
			select.addAttribute("name", selectName);
			select.addAttribute("onchange", "submit();");
			if (emptyOption) {
				select.add(new AOption("", ""));
			}
			for (String o : fo.keySet()) {
				select.add(new AOption(fo.get(o), o.toString()));
	    	}
			s2.add(select);
			p.add(s2);
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

	public void setSelected(String value) {
		String d = descriptions.get(value);
		desc.clear();
    	if (d!=null && !"".equals(d)) {
    		desc.add(new ALabel(d));
    	}
		select.setSelected(value);
	}
	
	public void setValue(String value) {
		in.addAttribute("value", value);
	}

	@Override
	protected String getTagName() {
		return "form";
	}

}
