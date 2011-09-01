package com.googlecode.e2u;

import java.util.Map;

import org.daisy.paper.Paper;

import com.googlecode.ajui.AInput;
import com.googlecode.ajui.ASelect;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;

public class AddSheetPaperComponent extends AddPaperComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -838591092138783249L;
	private final ASelect unitInput;
	private final AInput widthInput;
	private final ASelect unitHInput;
	private final AInput heightInput;

	public AddSheetPaperComponent(Map<String, String> fo, String selectName, boolean emptyOption, String target) {
		super(fo, "New sheet paper", selectName, emptyOption, target, Paper.Type.SHEET);

		unitInput = new ASelect();
		widthInput = new AInput();
		div.add(newLengthInput(fo, "Width", "width", "width-units", unitInput, widthInput, true));
		unitHInput = new ASelect();
		heightInput = new AInput();
		div.add(newLengthInput(fo, "Height", "height", "height-units", unitHInput, heightInput, true));
	}

	@Override
	public XHTMLTagger getHTML(Context context) {
		widthInput.addAttribute("value", context.getArgs().get("width"));
		unitInput.setSelected(context.getArgs().get("width-units"));
		heightInput.addAttribute("value", context.getArgs().get("height"));
		unitHInput.setSelected(context.getArgs().get("height-units"));
		return super.getHTML(context);
	}

}
