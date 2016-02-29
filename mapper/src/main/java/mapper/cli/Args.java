package mapper.cli;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import mapper.core.BranchType;

public class Args {
	@Parameter(description = "\"Query path\" \"Ontology path\"")
	List<String> files = new ArrayList<>();

	@Parameter(names = { "-h", "--help" }, help = true, description = "Print this help")
	boolean help;

	@Parameter(names = { "-m", "--match" }, validateWith = PositiveInteger.class, description = "Number of best matches per branch to output")
	int match = 1;

	@Parameter(names = { "-b", "--branches" }, variableArity = true, description = "Branches to include. Space separated from list [topic, operation, data, format]. If ommitted, all branches are considered.")
	List<BranchType> branches = new ArrayList<>();

	@Parameter(names = { "-o", "--output" }, description = "File to write results to. If not specified or invalid, will be written to standard output.")
	String output = "";

	@Parameter(names = { "-r", "--report" }, description = "File to write HTML benchmark report to. It will contain metrics and comparisons to the manual mapping specified in the input query file.")
	String report = "";

	public List<String> getFiles() {
		return files;
	}

	public int getMatch() {
		return match;
	}

	public List<BranchType> getBranches() {
		return branches;
	}

	public String getOutput() {
		return output;
	}
}
