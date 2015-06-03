package com.googlecode.e2u;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.googlecode.ajui.AContainer;

public abstract class AbstractSettingsView extends AContainer {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7230827450673663822L;

	protected String select(Selectable sc, String cval) {
		if (cval!=null) {
			try {
				cval = URLDecoder.decode(cval, MainPage.ENCODING);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			sc.setSelected(cval);
			return cval;
		} else {
			return "";
		}
    }
}
