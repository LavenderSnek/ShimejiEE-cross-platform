package com.group_finity.mascot.mac.jna;

import com.sun.jna.Structure;

public class ProcessSerialNumber extends Structure {
	public long highLongOfPSN, lowLongOfPSN;
}
