package com.googlecode.e2u;

import java.text.MessageFormat;
import java.util.List;

import org.daisy.braille.pef.PEFBook;
import org.daisy.dotify.api.validity.ValidatorMessage;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ADefinitionDescription;
import com.googlecode.ajui.ADefinitionList;
import com.googlecode.ajui.ADefinitionTerm;
import com.googlecode.ajui.AHeading;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class AboutBookView extends AContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4158397890850715579L;

	public AboutBookView(PEFBook book, List<ValidatorMessage> messages, MenuSystem menu) {
		if (menu!=null) {
			add(menu);
		}
		
		if (!messages.isEmpty()) {
			add(ValidationView.buildMessagesList(messages));
		}

		Iterable<String> data = book.getTitle();
		if (data==null || !data.iterator().hasNext()) {
			
		} else {
			for (String s: data) {
				AHeading a = new AHeading(2);
				a.add(new ALabel(s));
				add(a);
			}
		}
		data = book.getAuthors();
		if (data==null || !data.iterator().hasNext()) {
			
		} else {
			StringBuilder sb = new StringBuilder();
			String delimiter = "";
			for (String s : data) {
				sb.append(delimiter + s);
				delimiter = ", ";
			}
			AParagraph p = new AParagraph();
			p.add(new ALabel(sb.toString()));
			add(p);
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
		add(dl);
		dl = new ADefinitionList();
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

    	AParagraph p = new AParagraph();
    	ALink a = new ALink("#");
    	a.addAttribute("onclick", "window.open('book.xml','source'); return false;");
    	a.add(new ALabel(Messages.getString(L10nKeys.XSLT_VIEW_SOURCE)));
    	p.add(a);
    	add(p);
	}

}
