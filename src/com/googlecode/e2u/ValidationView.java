package com.googlecode.e2u;

import java.io.IOException;
import java.io.InputStreamReader;

import org.daisy.braille.api.validator.Validator;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.APre;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class ValidationView extends AContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7532480151718185817L;

	public ValidationView(String messages) {
		if (messages!=null) {
			{
				AParagraph p = new AParagraph();
				p.add(new ALabel(Messages.getString(L10nKeys.VALIDATION_MESSAGE)));
				add(p);
			}
			{
				AParagraph p = new AParagraph();
				p.add(new ALabel(Messages.getString(L10nKeys.CONTENT_PROVIDER)));
				add(p);
			}
			{
				AContainer div = new AContainer();
				//div.setClass("overflow");
				APre pre = new APre();
				pre.add(new ALabel(messages));
				div.add(pre);
				add(div);
		    	AParagraph p = new AParagraph();
		    	ALink a = new ALink("#");
		    	a.addAttribute("onclick", "window.open('book.xml','source'); return false;");
		    	a.add(new ALabel(Messages.getString(L10nKeys.XSLT_VIEW_SOURCE)));
		    	p.add(a);
		    	add(p);
			}
		} else {
			AParagraph p = new AParagraph();
			p.add(new ALabel(Messages.getString(L10nKeys.VALIDATION_FAILED)));
			add(p);
		}
	}
}
