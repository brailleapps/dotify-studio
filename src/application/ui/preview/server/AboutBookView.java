package application.ui.preview.server;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.streamline.api.validity.ValidatorMessage;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ADefinitionDescription;
import com.googlecode.ajui.ADefinitionList;
import com.googlecode.ajui.ADefinitionTerm;
import com.googlecode.ajui.AHeading;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;

import application.l10n.Messages;

public class AboutBookView extends AContainer {

	public AboutBookView(PEFBook book, List<ValidatorMessage> messages) {
		
		if (!messages.isEmpty()) {
			add(buildMessagesList(messages));
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
		
		List<Item> m = toItemList(book);

		ADefinitionList dl = new ADefinitionList();
		for (Item entry : m) {
			ADefinitionTerm dt = new ADefinitionTerm();
			dt.add(new ALabel(entry.getTitle()));
			dl.add(dt);
			for (String value : entry.getValues()) {
				ADefinitionDescription dd = new ADefinitionDescription();
				dd.add(new ALabel(value));
				dl.add(dd);
			}
		}
		add(dl);
		AParagraph p = new AParagraph();
		ALink a = new ALink("#");
		a.addAttribute("onclick", "window.open('book.xml','source'); return false;");
		a.add(new ALabel(Messages.XSLT_VIEW_SOURCE.localize()));
		p.add(a);
		add(p);
	}
	
	private static class Item {
		private final String title;
		private final List<String> dd = new ArrayList<>();
		Item(String title) {
			this.title = title;
		}
		
		void add(String value) {
			dd.add(value);
		}
		
		String getTitle() {
			return title;
		}
		
		List<String> getValues() {
			return dd;
		}
	}
	
	private static List<Item> toItemList(PEFBook book) {
		List<Item> dl = new ArrayList<>();
		{
			Item dd = new Item(Messages.SIZE.localize());
			dl.add(dd);
			dd.add(MessageFormat.format(Messages.SIZE_PAGES.localize(), book.getPages()));
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
			dd.add(MessageFormat.format(Messages.SIZE_SHEETS.localize(), book.getSheets(), s));
			dd.add(MessageFormat.format(Messages.SIZE_VOLUMES.localize(), book.getVolumes()));
		}
		{
			Item dd = new Item(Messages.DIMENSIONS.localize());
			dl.add(dd);
			dd.add(MessageFormat.format(Messages.FILE_DIMENSIONS.localize(), book.getMaxWidth(), book.getMaxHeight()));
		}
		{
			Item dd = new Item(Messages.DUPLEX.localize());
			float ratio = book.getPages()/(float)book.getPageTags();
			String info;
			if (ratio<=1) {
				info = Messages.DUPLEX_YES.localize();
			} else if (ratio>=2) {
				info = Messages.DUPLEX_NO.localize();
			} else {
				info = Messages.DUPLEX_MIXED.localize();
			}
			dd.add(info);
			dl.add(dd);
		}
		{
			Item dd = new Item(Messages.EIGHT_DOT.localize());
			dd.add((book.containsEightDot() ? Messages.YES.localize() : Messages.NO.localize()));
			dl.add(dd);
		}

		// This item retains the visual separation between the items above and the ones that follow. It can be removed.
		dl.add(new Item("\u00A0"));

		for (String key : book.getMetadataKeys()) {
			Item dd = new Item(Messages.getString("Worker.dc."+key));
			dl.add(dd);
			for (String value : book.getMetadata(key)) {
				dd.add(value);
			}
		}
		return dl;
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
