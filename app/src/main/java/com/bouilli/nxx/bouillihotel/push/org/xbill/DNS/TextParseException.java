// Copyright (c) 2002-2004 Brian Wellington (bwelling@xbill.org)

package com.bouilli.nxx.bouillihotel.push.org.xbill.DNS;

import java.io.IOException;

/**
 * An exception thrown when unable to parse text.
 *
 * @author Brian Wellington
 */

public class TextParseException extends IOException {
	private static final long serialVersionUID = 1L;

public
TextParseException() {
	super();
}

public
TextParseException(String s) {
	super(s);
}

}
