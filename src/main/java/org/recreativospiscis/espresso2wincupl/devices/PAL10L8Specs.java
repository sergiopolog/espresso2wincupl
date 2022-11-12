package org.recreativospiscis.espresso2wincupl.devices;

public class PAL10L8Specs implements PALSpecs {

	public static final String PAL_TYPE = "16L8";

	// @formatter:off
    private static final String[] LABELS_IN =  {  "i1",  "i2",  "i3",  "i4",  "i5",  "i6",  "i7",  "i8",  "i9", "i11",   null,   null,   null,   null,   null,   null,   null,   null };
    private static final String[] LABELS_IO =  { };
    private static final String[] LABELS_O  =  {  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "o18",  "o17",   "o16",  "o15",  "o14",  "o13",  "o19",  "o12" };
	// @formatter:on

	@Override
	public String toString() {
		return "PAL" + PAL_TYPE;
	}

	@Override
	public String getDevicePnemonic() {
		return "g16v8a";
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