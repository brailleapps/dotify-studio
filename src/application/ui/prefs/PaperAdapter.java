package application.ui.prefs;

import org.daisy.braille.utils.api.paper.Length;
import org.daisy.braille.utils.api.paper.Paper;
import org.daisy.braille.utils.api.paper.RollPaper;
import org.daisy.braille.utils.api.paper.SheetPaper;
import org.daisy.braille.utils.api.paper.TractorPaper;

import application.common.NiceName;
import application.l10n.Messages;

class PaperAdapter extends NiceName implements Comparable<PaperAdapter> {
	private final Paper p;
	private final String str;
	PaperAdapter(Paper p) {
		super(p.getIdentifier(), p.getDisplayName(), p.getDescription());
		this.p = p;
		switch (p.getType()) {
		case ROLL: {
			RollPaper rp = p.asRollPaper();
			this.str = Messages.MESSAGE_PAPER_DETAILS.localize(p.getDisplayName(), p.getDescription(), Messages.LABEL_ROLL_PAPER.localize(), toString(rp.getLengthAcrossFeed()));
			break; }
		case SHEET: {
			SheetPaper sp = p.asSheetPaper();
			this.str = Messages.MESSAGE_PAPER_DETAILS.localize(p.getDisplayName(), p.getDescription(), Messages.LABEL_SHEET_PAPER.localize(), toDim(sp.getPageWidth(), sp.getPageHeight()));
			break; }
		case TRACTOR: { 
			TractorPaper tp = p.asTractorPaper();
			this.str = Messages.MESSAGE_PAPER_DETAILS.localize(p.getDisplayName(), p.getDescription(), Messages.LABEL_TRACTOR_PAPER.localize(), toDim(tp.getLengthAcrossFeed(), tp.getLengthAlongFeed()));
			break; }
		default:
			this.str = "";
		}
	}
	
	private String toDim(Length l1, Length l2) {
		return toString(l1) + " x " + toString(l2);
	}
	
	private String toString(Length len) {
		return len.getLength() + " " + len.getUnitsOfLength();
	}
	
	Paper getPaper() {
		return p;
	}
	
	@Override
	public int compareTo(PaperAdapter o) {
		return p.getDisplayName().compareTo(o.p.getDisplayName());
	}

	@Override
	public String toString() {
		return str;
	}

}