package com.googlecode.e2u;

import java.util.Map;

import org.daisy.paper.Paper;

import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ASelect;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;

public class AddRollPaperComponent extends AddPaperComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -838591092138783249L;
	private final ASelect unitInput;
	private final AInput widthInput;

	public AddRollPaperComponent(Map<String, String> fo, String selectName, boolean emptyOption, String target) {
		super(fo, "New roll paper", selectName, emptyOption, target, Paper.Type.ROLL);

		unitInput = new ASelect();
		widthInput = new AInput();
		div.add(newLengthInput(fo, "Length", "width", "width-units", unitInput, widthInput, true));
	}

	@Override
	public XHTMLTagger getHTML(Context context) {
		widthInput.addAttribute("value", context.getArgs().get("width"));
		unitInput.setSelected(context.getArgs().get("width-units"));
		return super.getHTML(context);
	}

}
