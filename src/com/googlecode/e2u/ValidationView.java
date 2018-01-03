package com.googlecode.e2u;

import java.text.MessageFormat;
import java.util.List;

import org.daisy.streamline.api.validity.ValidatorMessage;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ADefinitionDescription;
import com.googlecode.ajui.ADefinitionList;
import com.googlecode.ajui.ADefinitionTerm;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class ValidationView extends AContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7532480151718185817L;

	public ValidationView(List<ValidatorMessage> messages) {
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
				if (!messages.isEmpty()) {
					div.add(buildMessagesList(messages));
				}
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
	
	static ADefinitionList buildMessagesList(List<ValidatorMessage> messages) {
		ADefinitionList dl = new ADefinitionList();
		for (ValidatorMessage vm : messages) {
			ADefinitionTerm dt = new ADefinitionTerm();
			if (vm.getLineNumber()>-1 && vm.getColumnNumber()>-1) {
				dt.add(new ALabel(MessageFormat.format("{0} at line {1}, column {2}", vm.getType(), vm.getLineNumber(), vm.getColumnNumber())));
			} else {
				dt.add(new ALabel(vm.getType().toString()));
			}
			dl.add(dt);
			ADefinitionDescription dd = new ADefinitionDescription();
			dd.add(new ALabel(
					vm.getMessage()
					.orElse(vm.getException().map(e->e.getMessage()).orElse("[No message]"))
				));
			dl.add(dd);
		}
		return dl;
	}
}
