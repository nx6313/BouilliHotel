// Copyright (c) 2003-2004 Brian Wellington (bwelling@xbill.org)

package com.bouilli.nxx.bouillihotel.push.org.xbill.DNS;

/**
 * An exception thrown when an invalid dclass code is specified.
 *
 * @author Brian Wellington
 */

public class InvalidDClassException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

public
InvalidDClassException(int dclass) {
	super("Invalid DNS class: " + dclass);
}

}
