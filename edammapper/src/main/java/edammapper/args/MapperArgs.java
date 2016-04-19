package edammapper.args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

import edammapper.edam.Branch;

public class MapperArgs {
	@Parameter(names = { "-b", "--branches" }, variableArity = true, description = "Branches to include. Space separated from list [topic, operation, data, format].")
	private List<Branch> branches = new ArrayList<>(Arrays.asList(Branch.topic, Branch.operation));

	@Parameter(names = { "-m", "--match" }, validateWith = PositiveInteger.class, description = "Number of best matches per branch to output")
	private int match = 3;

	@Parameter(names = { "--obsolete" }, description = "Include obsolete concepts")
	private boolean obsolete = false;

	@ParametersDelegate
	private MapperAlgorithmArgs mapperAlgorithmArgs = new MapperAlgorithmArgs();

	public int getMatch() {
		return match;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public boolean getObsolete() {
		return obsolete;
	}

	public MapperAlgorithmArgs algo() {
		return mapperAlgorithmArgs;
	}
}
