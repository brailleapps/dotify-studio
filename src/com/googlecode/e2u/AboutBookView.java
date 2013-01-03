package com.googlecode.e2u;

import java.text.MessageFormat;

import org.daisy.braille.pef.PEFBook;


import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ADefinitionDescription;
import com.googlecode.ajui.ADefinitionList;
import com.googlecode.ajui.ADefinitionTerm;
import com.googlecode.ajui.ALabel;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class AboutBookView extends AContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4158397890850715579L;

	public AboutBookView(PEFBook book, MenuSystem menu) {
		if (menu!=null) {
			add(menu);
		}
		ADefinitionList dl = new ADefinitionList();
		{
			ADefinitionTerm dt = new ADefinitionTerm();
			dt.add(new ALabel(Messages.getString(L10nKeys.SIZE)));
			dl.add(dt);
		}
		{	
			ADefinitionDescription dd = new ADefinitionDescription();
			dd.add(new ALabel(MessageFormat.format(Messages.getString(L10nKeys.SIZE_PAGES), book.getPages())));
			dl.add(dd);
		}
		{
	   		StringBuilder s = new StringBuilder();
	    	for (int i=1; i<=book.getVolumes(); i++) {
	    		if (i==1) {
	    			//s.append("(");
	    		}
	    		s.append(book.getSheets(i));
	    		if (i<book.getVolumes()) {
	    			s.append(" + ");
	    		} else {
	    			//s.append(")");
	    		}
	    	}
			{	
				ADefinitionDescription dd = new ADefinitionDescription();
				dd.add(new ALabel(MessageFormat.format(Messages.getString(L10nKeys.SIZE_SHEETS), book.getSheets(), s)));
				dl.add(dd);
			}
		}
		{	
			ADefinitionDescription dd = new ADefinitionDescription();
			dd.add(new ALabel(MessageFormat.format(Messages.getString(L10nKeys.SIZE_VOLUMES), book.getVolumes())));
			dl.add(dd);
		}
		{
			ADefinitionTerm dt = new ADefinitionTerm();
			dt.add(new ALabel(Messages.getString(L10nKeys.DIMENSIONS)));
			dl.add(dt);
		}
		{	
			ADefinitionDescription dd = new ADefinitionDescription();
			dd.add(new ALabel(MessageFormat.format(Messages.getString(L10nKeys.FILE_DIMENSIONS), book.getMaxWidth(), book.getMaxHeight())));
			dl.add(dd);
		}
		{
			ADefinitionTerm dt = new ADefinitionTerm();
			dt.add(new ALabel(Messages.getString(L10nKeys.DUPLEX)));
			dl.add(dt);
		}
		{	
			ADefinitionDescription dd = new ADefinitionDescription();
			float ratio = book.getPages()/(float)book.getPageTags();
			String info;
			if (ratio<=1) {
				info = Messages.getString(L10nKeys.DUPLEX_YES);
			} else if (ratio>=2) {
				info = Messages.getString(L10nKeys.DUPLEX_NO);
			} else {
				info = Messages.getString(L10nKeys.DUPLEX_MIXED);
			}
			dd.add(new ALabel(info));
			dl.add(dd);
		}
		{
			ADefinitionTerm dt = new ADefinitionTerm();
			dt.add(new ALabel(Messages.getString(L10nKeys.EIGHT_DOT)));
			dl.add(dt);
		}
		{	
			ADefinitionDescription dd = new ADefinitionDescription();
			dd.add(new ALabel((book.containsEightDot() ? Messages.getString(L10nKeys.YES) : Messages.getString(L10nKeys.NO))));
			dl.add(dd);
		}
    	for (String key : book.getMetadataKeys()) {
    		{
    			ADefinitionTerm dt = new ADefinitionTerm();
    			dt.add(new ALabel(Messages.getString("Worker.dc."+key)));
    			dl.add(dt);
    		}
    		for (String value : book.getMetadata(key)) {
        		{	
        			ADefinitionDescription dd = new ADefinitionDescription();
        			dd.add(new ALabel(value));
        			dl.add(dd);
        		}
    		}
    	}
    	add(dl);
	}

}
