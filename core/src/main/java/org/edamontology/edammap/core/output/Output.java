/*
 * Copyright © 2016, 2017, 2018 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.output;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.FetcherCommon;
import org.edamontology.pubfetcher.Publication;
import org.edamontology.pubfetcher.Version;
import org.edamontology.pubfetcher.Webpage;

public class Output {

	private final Path output;

	private final Path report;

	private final boolean existingDirectory;

	public Output(String output, String report, boolean existingDirectory) throws IOException {
		this.output = (output == null || output.isEmpty()) ? null : FetcherCommon.outputPath(output);

		this.report = (report == null || report.isEmpty()) ? null : FetcherCommon.outputPath(report, true, existingDirectory);

		this.existingDirectory = existingDirectory;
	}

	public void output(CoreArgs args, List<Param> paramsMain, QueryType type, int reportPageSize, int reportPaginationSize, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Webpage>> webpages, List<List<Webpage>> docs, List<List<Publication>> publications, Results results, long start, long stop, Version version) throws IOException {
		Txt.output(type, output, report, concepts, queries, publications, results.getMappings());
		Report.output(args, paramsMain, type, reportPageSize, reportPaginationSize, report, existingDirectory, concepts, queries, publications, webpages, docs, results, start, stop, version);
	}
}
