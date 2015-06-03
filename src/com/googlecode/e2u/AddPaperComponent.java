package com.googlecode.e2u;

import java.util.Map;

import org.daisy.paper.Paper;

import com.googlecode.ajui.ABlockComponent;
import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.AHeading;
import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.AOption;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.ASelect;
import com.googlecode.ajui.ASpan;
import com.googlecode.ajui.AbstractComponent;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public abstract class AddPaperComponent extends AbstractComponent<ABlockComponent> implements ABlockComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6590046424943180089L;
	protected AContainer div;
	private final AInput name;
	private final AInput desc;


	public AddPaperComponent(Map<String, String> fo, String heading, String selectName, boolean emptyOption, String target, Paper.Type type) {
		addAttribute("action", "/index.html");
		addAttribute("method", "get");
		div = new AContainer();
		{
			{
				AHeading h = new AHeading(2);
				h.add(new ALabel(heading));
				div.add(h);
			}
			{
				name = new AInput();
				name.addAttribute("type", "text");
				name.addAttribute("name", "name");
				AParagraph p = new AParagraph();
				p.addAttribute("class", "newPaper");
				ASpan s = new ASpan();
				s.setClass("settingName");
				s.add(new ALabel(Messages.getString(L10nKeys.PAPER_NAME)+" "));
				p.add(s);
				p.add(name);
				div.add(p);
			}
			{
				desc = new AInput();
				desc.addAttribute("type", "text");
				desc.addAttribute("name", "desc");
				AParagraph p = new AParagraph();
				p.addAttribute("class", "newPaper");
				ASpan s = new ASpan();
				s.setClass("settingName");
				s.add(new ALabel(Messages.getString(L10nKeys.PAPER_DESCRIPTION) + " "));
				p.add(s);
				p.add(desc);
				div.add(p);
			}
			add(div);
		}
		{
			AParagraph p = new AParagraph();
			AInput input = new AInput();
			input.addAttribute("type", "hidden");
			input.addAttribute("name", "method");
			input.addAttribute("value", target);
			p.add(input);
			add(p);
		}
		{
			AParagraph p = new AParagraph();
			AInput input = new AInput();
			input.addAttribute("type", "hidden");
			input.addAttribute("name", "type");
			input.addAttribute("value", type.name().toString());
			p.add(input);
			add(p);
		}
		{
			AInput submit = new AInput();
			submit.addAttribute("type", "submit");
			submit.addAttribute("value", Messages.getString(L10nKeys.ADD));
			AParagraph p = new AParagraph();
			p.add(submit);
			add(p);
		}
	}
	
	protected ABlockComponent newLengthInput(Map<String, String> fo, String displayName, String valueName, String unitName, ASelect select, AInput in, boolean emptyOption) {
		AParagraph p = new AParagraph();
		{
			ASpan s = new ASpan();
			s.setClass("settingName");
			s.add(new ALabel(displayName + " "));
			p.add(s);
		}
		{
			ASpan s = new ASpan();
			s.setClass("length");
	
			in.addAttribute("type", "text");
			in.addAttribute("name", valueName);
			s.add(in);
			p.add(s);
	
			select.addAttribute("name", unitName);
			if (emptyOption) {
				select.add(new AOption("", ""));
			}
			for (String o : fo.keySet()) {
				select.add(new AOption(fo.get(o), o.toString()));
	    	}
			s.add(select);
		}
		return p;
	}


	@Override
	protected String getTagName() {
		return "form";
	}

	@Override
	public XHTMLTagger getHTML(Context context) {
		name.addAttribute("value", context.getArgs().get("name"));
		desc.addAttribute("value", context.getArgs().get("desc"));
		return super.getHTML(context);
	}

}
