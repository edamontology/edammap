package edammapper.args;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ZeroToOneDouble implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		double n = Double.parseDouble(value);
		if (n < 0 || n > 1) {
			throw new ParameterException("Parameter " + name + " should be between 0 and 1 (found " + value + ")");
		}
	}
}
