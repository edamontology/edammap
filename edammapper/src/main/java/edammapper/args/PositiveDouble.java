package edammapper.args;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class PositiveDouble implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		double n = Double.parseDouble(value);
		if (n < 0) {
			throw new ParameterException("Parameter " + name + " should be positive (found " + value + ")");
		}
	}
}
