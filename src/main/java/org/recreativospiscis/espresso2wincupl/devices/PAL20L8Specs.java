package org.recreativospiscis.espresso2wincupl.devices;

public class PAL20L8Specs implements PALSpecs {

	public static final String PAL_TYPE = "20L8";

	// @formatter:off
    private static final String[] LABELS_IN =  {  "i1",  "i2",  "i3",  "i4",  "i5",  "i6",  "i7",  "i8",  "i9", "i10",   null,   null,   null,   null,   null,   null,   null,   null,  "i11",  "i13",  "i14",  "i23" };
    private static final String[] LABELS_IO =  {  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,   null, "io16", "io17", "io18", "io19", "io20", "io21",   null,   null,   null,   null,   null };
    private static final String[] LABELS_O  = {   null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "o15",   null,   null,   null,   null,   null,   null,  "o22",   null,   null,   null,   null };
	// @formatter:on

	@Override
	public String toString() {
		return "PAL" + PAL_TYPE;
	}

	@Override
	public String getDevicePnemonic() {
		return "g20v8a";
	}

	@Override
	public String[] getLabels_IN() {
		return LABELS_IN;
	}

	@Override
	public String[] getLabels_O() {
		return LABELS_O;
	}

	@Override
	public String[] getLabels_IO() {
		return LABELS_IO;
	}

}