package edammapper.input.json;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Function {

	private List<Edam> operation = new ArrayList<>();

	private List<InputOutput> input = new ArrayList<>();

	private List<InputOutput> output = new ArrayList<>();

	private String comment;

	public void check(ToolInput tool, int i, String index) throws ParseException {
		if (operation == null || operation.isEmpty()) {
			tool.parseException("operation", i, index);
		} else {
			for (int j = 0; j < operation.size(); ++j) {
				operation.get(j).check(tool, i, index + ", operation " + j);
			}
		}
		if (input != null) {
			for (int j = 0; j < input.size(); ++j) {
				input.get(j).check(tool, i, index + ", input " + j);
			}
		}
		if (output != null) {
			for (int j = 0; j < output.size(); ++j) {
				output.get(j).check(tool, i, index + ", output " + j);
			}
		}
	}

	public List<Edam> getOperation() {
		return operation;
	}
	public void setOperation(List<Edam> operation) {
		this.operation = operation;
	}

	public List<InputOutput> getInput() {
		return input;
	}
	public void setInput(List<InputOutput> input) {
		this.input = input;
	}

	public List<InputOutput> getOutput() {
		return output;
	}
	public void setOutput(List<InputOutput> output) {
		this.output = output;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
