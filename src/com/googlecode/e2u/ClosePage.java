package com.googlecode.e2u;




import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class ClosePage extends BasePage {
	
        @Override
	public String getContentString(String key, Context context) {
		if (KEY_TITLE.equals(key)) {
			return Messages.getString(L10nKeys.CLOSED);
		}
		String ret = buildHTML(closeHTML(), Messages.getString(L10nKeys.CLOSED), true);
		context.close();
		return ret; 
	}

    private String closeHTML() {
    	return new XHTMLTagger().tag("p", Messages.getString(L10nKeys.TOOLTIP_CLOSED)).getResult(); //$NON-NLS-1$
    }

	@Override
	public void close() {
		// Nothing to do
	}

}
