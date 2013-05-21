package biz.wolschon.cam.multiaxis.strategy;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Final strategy in the chain of command.<br/>
 * Writes the actual G-Code to a file.
 */
public class GCodeWriterStrategy implements IStrategy {

	/*
	 * Where we write our G-Code to.
	 */
	private Writer mOut;

	/**
	 * Number format to use for G-Code.
	 */
	private static final DecimalFormat NUMBERFORMAT;
	
	static {
		DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.ENGLISH);
		decimalSymbol.setDecimalSeparator('.');
		NUMBERFORMAT = new DecimalFormat("#.########", decimalSymbol);
		NUMBERFORMAT.setGroupingUsed(false);
	}
	
	/**
	 * @param aOutput where we write our G-Code to.
	 */
	public GCodeWriterStrategy(final Writer aOutput) {
		this.mOut = aOutput;
		//TODO: write a G-Code header (use metric coordinates, spin up the spindle)
	}

	/**
	 * Write a G1 movement command to reach the given tool location.
	 * @param aNextToolLocation the (3-6 dimensional) location we are trying to move to.
	 */
	@Override
	public void runStrategy(final double[] aNextToolLocation) throws IOException {
		StringBuilder sb = new StringBuilder();

		sb.append("G1 X").append(formatCoordinate(aNextToolLocation[0]));
		sb.append(" Y").append(formatCoordinate(aNextToolLocation[1]));
		sb.append(" Z").append(formatCoordinate(aNextToolLocation[2]));
		if (aNextToolLocation.length > 3) {
			sb.append(" A").append(formatCoordinate(aNextToolLocation[3]));
		}
		if (aNextToolLocation.length > 4) {
			sb.append(" B").append(formatCoordinate(aNextToolLocation[4]));
		}
		sb.append("\n");
		writeCodeLine(sb.toString(), aNextToolLocation);
		
		System.out.print(sb.toString()); //TODO: debug output
		
	}

	private String formatCoordinate(final double aNextToolLocation) {
		if (Double.isInfinite(aNextToolLocation)) {
			throw new IllegalArgumentException("cannot format INFINITE");
		}
		if (Double.isNaN(aNextToolLocation)) {
			throw new IllegalArgumentException("cannot format NotANumber");
		}
		//TODO: limit the number of decimal places
 		return NUMBERFORMAT.format(aNextToolLocation);
	}

	/**
	 * You may overwrite this method to intercept the G-Code before it is written.
	 * @param aLine the actual G-Code being written
	 * @param aLocation the machine location encoded in the movement comment (for convenience)
	 */
	protected void writeCodeLine(final String aLine, final double[] aLocation) throws IOException {
		mOut.write(aLine);
	}

	/**
	 * Write the footer but do not close the output writer (since we didn't open it).
	 */
	@Override
	public void endStrategy()  throws IOException {
		//TODO: write G-Code footer (spin down spindle, stop program)
	}
}
