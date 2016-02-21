package mapper.cli;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class Args {
	@Parameter(description = "\"Query path\" \"Ontology path\"")
	List<String> files = new ArrayList<>();

	@Parameter(names = { "-h", "--help" }, description = "Print this help", help = true)
	boolean help;
}
