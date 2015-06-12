package com.googlecode.e2u;

import java.io.IOException;
import java.text.MessageFormat;

import org.daisy.braille.api.paper.CustomPaperCollection;
import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.Paper;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.ASpan;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class PaperView extends AContainer {
	private final CustomPaperCollection coll;
	private final AddPaperComponent newRollPaper;
	private final AddPaperComponent newTractorPaper;
	private final AddPaperComponent newSheetPaper;
	private final AContainer addNew;
	private final static String paperTarget = "paper";
	private final MenuSystem menu;
	private final AListener listener;
	/**
	 * 
	 */
	private static final long serialVersionUID = -4415556019420390748L;

	public PaperView(MenuSystem menu, AListener listener) {
		coll = CustomPaperCollection.getInstance();
		this.menu = menu;
		this.listener = listener;

		newRollPaper = new AddRollPaperComponent(SettingsView.lengthNN, "rollPaper", true, paperTarget);
		newTractorPaper = new AddTractorPaperComponent(SettingsView.lengthNN, "tractorPaper", true, paperTarget);
		newSheetPaper = new AddSheetPaperComponent(SettingsView.lengthNN, "sheetPaper", true, paperTarget);

		addNew = new AContainer();
		{
			AParagraph p = new AParagraph();
			{
				ALink a = new ALink("?method=paper&add=sheet-paper");
				a.add(new ALabel("[+] " + Messages.getString(L10nKeys.CUT_SHEET_PAPER)));
				p.add(a);
			}
			{
				p.add(new ALabel(" | "));
				ALink a = new ALink("?method=paper&add=tractor-paper");
				a.add(new ALabel("[+] "+ Messages.getString(L10nKeys.TRACTOR_PAPER)));
				p.add(a);
			}
			{
				p.add(new ALabel(" | "));
				ALink a = new ALink("?method=paper&add=roll-paper");
				a.add(new ALabel("[+] " + Messages.getString(L10nKeys.ROLL_PAPER)));
				p.add(a);
			}
			addNew.add(p);
		}/*
		{
			AParagraph p = new AParagraph();
			ALink a = new ALink("?method=paper&add=tractor-paper");
			a.add(new ALabel("[+] Tractor paper"));
			p.add(a);
			addNew.add(p);
		}
		{
			AParagraph p = new AParagraph();
			ALink a = new ALink("?method=paper&add=roll-paper");
			a.add(new ALabel("[+] Roll paper"));
			p.add(a);
			addNew.add(p);
		}
*/
	}

	@Override
	public XHTMLTagger getHTML(Context context) {
		//update
		String remove = context.getArgs().get("remove");
		if (remove!=null && !"".equals(remove)) {
			Paper r = null;
			for (Paper paper : coll.list()) {
				if (paper.getIdentifier().equals(remove)) {
					r = paper;
					break;
				}
			}
			if (r!=null) {
				try {
					coll.remove(r);
					listener.changeHappened(this);
				} catch (IOException e) {
				}
			}
		}
		String type = context.getArgs().get("type");
		boolean updateOK = true;
		try {
			if (type!=null && !"".equals(type)) {
				updateOK = false;
				String name = context.getArgs().get("name");
				String desc = context.getArgs().get("desc");
				Paper.Type t = Paper.Type.valueOf(type);
				if (name!=null && !"".equals(name)) {
					switch (t) {
						case ROLL:
							coll.addNewRollPaper(name, desc, getLength(context.getArgs().get("width"), context.getArgs().get("width-units")));
							updateOK = true;
							break;
						case TRACTOR:
							coll.addNewTractorPaper(name, desc,
									getLength(context.getArgs().get("width"), context.getArgs().get("width-units")), 
									getLength(context.getArgs().get("height"), context.getArgs().get("height-units"))
									);
							updateOK = true;
							break;
						case SHEET:
							coll.addNewSheetPaper(name, desc,
									getLength(context.getArgs().get("width"), context.getArgs().get("width-units")), 
									getLength(context.getArgs().get("height"), context.getArgs().get("height-units"))
									);
							updateOK = true;
							break;
						default:
							break;
					}
				}
				if (updateOK) {
					listener.changeHappened(this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//update view
		clear();
		if (menu!=null) {
			add(menu);
		}
		if ((!updateOK&&type.equals(Paper.Type.ROLL.toString())) || "roll-paper".equals(context.getArgs().get("add"))) {
			add(newRollPaper);
		} else if ((!updateOK&&type.equals(Paper.Type.TRACTOR.toString())) || "tractor-paper".equals(context.getArgs().get("add"))) {
			add(newTractorPaper);
		} else if ((!updateOK&&type.equals(Paper.Type.SHEET.toString())) || "sheet-paper".equals(context.getArgs().get("add"))) {
			add(newSheetPaper);
		} else {
			for (Paper paper : coll.list()) {
				AParagraph p = new AParagraph();
				ALink a = new ALink(MainPage.TARGET+"?method=paper&remove="+paper.getIdentifier());
				a.addAttribute("title", Messages.getString(L10nKeys.REMOVE));
				a.addAttribute("onclick", "return confirm('"+MessageFormat.format(Messages.getString(L10nKeys.CONFIRM_REMOVE), paper.getDisplayName())+"')");
				ALabel label = new ALabel("[-] ");
				a.add(label);
				p.add(a);
				ASpan s = new ASpan();
				s.addAttribute("title", paper.toString());
				s.add(new ALabel(paper.getDisplayName()+ " " + paper.getDescription() + " "));
				p.add(s);
				add(p);
			}
			add(addNew);
		}
		return super.getHTML(context);
	}
	

	private Length getLength(String valStr, String unitStr) {
		Length.UnitsOfLength unit = Length.UnitsOfLength.valueOf(unitStr);
		double val = Double.parseDouble(valStr);
		switch (unit) {
			case CENTIMETER:
				return Length.newCentimeterValue(val);
			case MILLIMETER:
				return Length.newMillimeterValue(val);
			case INCH:
				return Length.newInchValue(val);
			default:
				throw new RuntimeException();
		}
	}
}
