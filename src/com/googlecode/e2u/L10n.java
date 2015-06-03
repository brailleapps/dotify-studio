package com.googlecode.e2u;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


import com.googlecode.ajui.Content;
import com.googlecode.ajui.Context;
import com.googlecode.e2u.l10n.Messages;

public class L10n implements Content {

	public Reader getContent(String key, Context context) throws IOException {
		return new StringReader(Messages.getString(key));
	}

	@Override
	public void close() {
		// nothing to do
	}

}
