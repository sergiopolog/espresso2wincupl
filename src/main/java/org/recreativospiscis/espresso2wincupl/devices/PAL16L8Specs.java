package org.recreativospiscis.espresso2wincupl.devices;

public class PAL16L8Specs implements PALSpecs {

	public static final String PAL_TYPE = "10L8";

	// @formatter:off
    private static final String[] LABELS_IN =  {  "i1",  "i2",  "i3",  "i4",  "i5",  "i6",  "i7",  "i8",  "i9", "i11",   null,   null,   null,   null,   null,   null,   null,  null };
    private static final String[] LABELS_IO =  {  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "io18", "io17", "io16", "io15", "io14", "io13",  null,  null };
    private static final String[] LABELS_O  =  {  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,   null,   null,   null,   null,   null,   null,  "o19",  "o12" };
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