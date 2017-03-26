// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package com.bouilli.nxx.bouillihotel.push.org.xbill.DNS;

/**
 * The Name Server Identifier Option, define in RFC 5001.
 *
 * @see OPTRecord
 * 
 * @author Brian Wellington
 */
public class NSIDOption extends GenericEDNSOption {

NSIDOption() {
	super(EDNSOption.Code.NSID);
}

/**
 * Construct an NSID option.
 * @param data The contents of the option.
 */
public 
NSIDOption(byte [] data) {
	super(EDNSOption.Code.NSID, data);
}

}
