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

import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;

import org.edamontology.edammap.core.args.ArgMain;
import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryType;

public class Output {

	private final Path txt;

	private final Path report;

	private final Path json;

	private final boolean existingDirectory;

	public Output(String txt, String report, String json, boolean existingDirectory) throws IOException {
		this.txt = (txt == null || txt.isEmpty()) ? null : PubFetcher.outputPath(txt);

		this.report = (report == null || report.isEmpty()) ? null : PubFetcher.outputPath(report, true, existingDirectory);

		this.json = (json == null || json.isEmpty()) ? null : PubFetcher.outputPath(json);

		this.existingDirectory = existingDirectory;
	}

	public void output(CoreArgs args, List<ArgMain> argsMain, Map<String, String> jsonFields, QueryType type, int reportPageSize, int reportPaginationSize, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Webpage>> webpages, List<List<Webpage>> docs, List<List<Publication>> publications, Results results, long start, long stop, Version version, String jsonVersion) throws IOException {
		Txt.output(type, txt, report, concepts, queries, publications, results.getMappings());
		Report.output(args, argsMain, type, reportPageSize, reportPaginationSize, report, existingDirectory, concepts, queries, publications, webpages, docs, results, start, stop, version, txt != null, json != null);
		if (json != null) {
			JsonType jsonType = (type == QueryType.server ? JsonType.full : JsonType.cli);
			Json.output(args, argsMain, jsonFields, type, jsonType, json, concepts, queries, publications, webpages, docs, results, start, stop, version, jsonVersion);
		}
	}
}
