package com.googlecode.e2u;

import com.googlecode.ajui.ABlockComponent;
import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.ASpan;
import com.googlecode.ajui.AbstractComponent;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;

public class InputSelectComponent extends AbstractComponent<ABlockComponent> implements ABlockComponent {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8741568092626224281L;
	private final AInput in;

	public InputSelectComponent(String inputName, String displayName, String target) {
		addAttribute("action", "/index.html");
		addAttribute("method", "get");
		AContainer div = new AContainer();
		{
			AParagraph p = new AParagraph();
			p.setClass("find");
			ASpan s = new ASpan();
			s.setClass("settingName");
			s.add(new ALabel(displayName));
			p.add(s);
			ASpan s2 = new ASpan();

			in = new AInput();
			in.addAttribute("type", "text");
			in.addAttribute("name", inputName);
			s2.add(in);
			p.add(s2);
			div.add(p);
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

	@Override
	public XHTMLTagger getHTML(Context context) {
		String find = context.getArgs().get("this");
		if (find!=null) {
			in.addAttribute("value", find);
		}
		return super.getHTML(context);
	}

	@Override
	protected String getTagName() {
		return "form";
	}

}
