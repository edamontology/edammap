package edammapper.output;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import edammapper.args.Args;
import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.mapping.Mapping;
import edammapper.query.Query;

public class Output {

	private Args args;

	private Path output;

	private Path report;

	private Path benchmarkReport;

	public Output(Args args) throws AccessDeniedException, FileAlreadyExistsException {
		this.args = args;

		this.output = check(args.getOutput());

		this.report = check(args.getReport());

		this.benchmarkReport = check(args.getBenchmarkReport());
	}

	private Path check(String file) throws AccessDeniedException, FileAlreadyExistsException {
		if (file != null && !file.isEmpty()) {
			Path path = Paths.get(file);
			Path parent = (path.getParent() != null ? path.getParent() : Paths.get("."));
			if (!Files.isDirectory(parent) || !Files.isWritable(parent)) {
				throw new AccessDeniedException(parent.toAbsolutePath().normalize() + " is not a writeable directory!");
			}
			if (Files.isDirectory(path)) {
				throw new FileAlreadyExistsException(path.toAbsolutePath().normalize() + " is an existing directory!");
			}
			return path;
		} else {
			return null;
		}
	}

	public void output(Map<EdamUri, Concept> concepts, List<Query> queries, List<Mapping> mappings) throws IOException {
		Txt.output(args.getType(), output, concepts, queries, mappings);
		Html.output(args, report, concepts, queries, mappings);
		Benchmark.output(args, benchmarkReport, concepts, queries, mappings);
	}
}
