package com.googlecode.e2u;

import java.io.IOException;
import java.io.InputStreamReader;

import org.daisy.validator.Validator;


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

	public ValidationView(Validator v) {
		if (v!=null) {
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
				InputStreamReader isr = new InputStreamReader(v.getReportStream());
				int c;
				try {
					StringBuilder sb = new StringBuilder();
					while ((c = isr.read())>-1) {
						sb.append(((char)c));
					}
					pre.add(new ALabel(sb.toString()));
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						isr.close();
					} catch (IOException e) {}
				}
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
