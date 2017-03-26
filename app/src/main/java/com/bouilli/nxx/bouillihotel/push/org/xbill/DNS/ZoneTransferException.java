// Copyright (c) 2003-2004 Brian Wellington (bwelling@xbill.org)

package com.bouilli.nxx.bouillihotel.push.org.xbill.DNS;

/**
 * An exception thrown when a zone transfer fails.
 *
 * @author Brian Wellington
 */

public class ZoneTransferException extends Exception {
	private static final long serialVersionUID = 1L;

public
ZoneTransferException() {
	super();
}

public
ZoneTransferException(String s) {
	super(s);
}

}
