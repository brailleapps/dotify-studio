package com.googlecode.e2u;

import java.util.Map;

import org.daisy.paper.Paper;

import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ASelect;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class AddRollPaperComponent extends AddPaperComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -838591092138783249L;
	private final ASelect unitInput;
	private final AInput widthInput;

	public AddRollPaperComponent(Map<String, String> fo, String selectName, boolean emptyOption, String target) {
		super(fo, Messages.getString(L10nKeys.ADD_NEW_ROLL_PAPER), selectName, emptyOption, target, Paper.Type.ROLL);

		unitInput = new ASelect();
		widthInput = new AInput();
		div.add(newLengthInput(fo, Messages.getString(L10nKeys.ROLL_SIZE), "width", "width-units", unitInput, widthInput, true));
	}

	@Override
	public XHTMLTagger getHTML(Context context) {
		widthInput.addAttribute("value", context.getArgs().get("width"));
		unitInput.setSelected(context.getArgs().get("width-units"));
		return super.getHTML(context);
	}

}
