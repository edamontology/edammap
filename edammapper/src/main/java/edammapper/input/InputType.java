package edammapper.input;

import java.text.ParseException;

public interface InputType {
	public void check(int i) throws ParseException;

	default void parseException(String attribute, int i, String index) throws ParseException {
		throw new ParseException("Attribute \"" + attribute + "\" missing or empty! (record " + index + ")", i);
	}

	default void parseException(String attribute, int i) throws ParseException {
		parseException(attribute, i, String.valueOf(i));
	}
}
