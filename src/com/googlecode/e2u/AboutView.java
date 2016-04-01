package com.googlecode.e2u;

import java.text.MessageFormat;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AListItem;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.AUnorderedList;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class AboutView extends AContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3999664278998065149L;

	public AboutView() {
		{
	    	AParagraph ap = new AParagraph();
	    	ap.add(new ALabel(Messages.getString(L10nKeys.DESC_SOFTWARE)));
	    	this.add(ap);
    	}
    	{
	    	AParagraph ap = new AParagraph();
	    	ap.add(new ALabel(Messages.getString(L10nKeys.DESC_MORE_INFO)));
	    	ALink link = new ALink("http://pef-format.org");
	    	link.addAttribute("target", "_blank");
	    	link.add(new ALabel("pef-format.org"));
	    	ap.add(link);
	    	this.add(ap);
    	}
    	{
	    	AParagraph ap = new AParagraph();
	    	ap.add(new ALabel(MessageFormat.format(Messages.getString(L10nKeys.DESC_VERSION), BuildInfo.VERSION, BuildInfo.BUILD)));
	    	this.add(ap);
    	}
    	{
	    	AParagraph ap = new AParagraph();
	    	ap.add(new ALabel(Messages.getString(L10nKeys.DESC_BASED_ON)));
	    	this.add(ap);
    	}
    	{
    		AUnorderedList list = new AUnorderedList();
    		{
    			AListItem li = new AListItem();
    			ALink link = new ALink("http://code.google.com/p/brailleutils/");
    			link.addAttribute("target", "_blank");
    			link.add(new ALabel("Braille Utils"));
    			li.add(link);
    			list.add(li);
    		}
    		{
    			AListItem li = new AListItem();
    			ALink link = new ALink("http://code.google.com/p/ajui/");
    			link.addAttribute("target", "_blank");
    			link.add(new ALabel("Accessible Java User Interface"));
    			li.add(link);
    			list.add(li);
    		}
    		this.add(list);
    	}
	}

}
