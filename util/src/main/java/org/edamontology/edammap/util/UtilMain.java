/*
 * Copyright © 2018 Erik Jaaniso
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

package org.edamontology.edammap.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.Fetcher;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherUtil;
import org.edamontology.pubfetcher.PublicationIds;

public final class UtilMain {

	private static List<PublicationIds> pubQuery(List<String> queryPaths, QueryType type, FetcherArgs fetcherArgs) throws IOException, ParseException {
		List<PublicationIds> publicationIds = new ArrayList<>();
		System.out.println("Load publication IDs from file " + queryPaths + " of type " + type);
		for (String queryPath : queryPaths) {
			publicationIds.addAll(QueryLoader.get(queryPath, type, fetcherArgs).stream()
				.flatMap(q -> q.getPublicationIds().stream()
					.map(id -> new PublicationIds(id.getPmid(), id.getPmcid(), id.getDoi(), id.getPmidUrl(), id.getPmcidUrl(), id.getDoiUrl())))
				.collect(Collectors.toList()));
		}
		System.out.println("Loaded " + publicationIds.size() + " publication IDs");
		return publicationIds;
	}

	private static List<String> webQuery(List<String> queryPaths, QueryType type, FetcherArgs fetcherArgs) throws IOException, ParseException {
		List<String> webpageUrls = new ArrayList<>();
		System.out.println("Load webpage URLs from file " + queryPaths + " of type " + type);
		for (String queryPath : queryPaths) {
			webpageUrls.addAll(QueryLoader.get(queryPath, type, fetcherArgs).stream()
				.flatMap(q -> q.getWebpageUrls().stream()
					.map(url -> url.getUrl()))
				.collect(Collectors.toList()));
		}
		System.out.println("Loaded " + webpageUrls.size() + " webpage URLs");
		return webpageUrls;
	}

	private static List<String> docQuery(List<String> queryPaths, QueryType type, FetcherArgs fetcherArgs) throws IOException, ParseException {
		List<String> docUrls = new ArrayList<>();
		System.out.println("Load doc URLs from file " + queryPaths + " of type " + type);
		for (String queryPath : queryPaths) {
			docUrls.addAll(QueryLoader.get(queryPath, type, fetcherArgs).stream()
				.flatMap(q -> q.getDocUrls().stream()
					.map(url -> url.getUrl()))
				.collect(Collectors.toList()));
		}
		System.out.println("Loaded " + docUrls.size() + " doc URLs");
		return docUrls;
	}

	public static void main(String[] argv) throws IOException, ParseException, ReflectiveOperationException {
		UtilArgs args = FetcherUtil.parseArgs(argv, UtilArgs.class);

		List<PublicationIds> publicationIds = null;
		List<String> webpageUrls = null;
		List<String> docUrls = null;

		if (args.pubQuery != null) publicationIds = pubQuery(args.pubQuery, args.queryType, args.fetcherArgs);
		if (args.webQuery != null) webpageUrls = webQuery(args.webQuery, args.queryType, args.fetcherArgs);
		if (args.docQuery != null) docUrls = docQuery(args.docQuery, args.queryType, args.fetcherArgs);
		if (args.allQuery != null) {
			if (publicationIds == null) publicationIds = pubQuery(args.allQuery, args.queryType, args.fetcherArgs);
			else publicationIds.addAll(pubQuery(args.allQuery, args.queryType, args.fetcherArgs));
			if (webpageUrls == null) webpageUrls = webQuery(args.allQuery, args.queryType, args.fetcherArgs);
			else webpageUrls.addAll(webQuery(args.allQuery, args.queryType, args.fetcherArgs));
			if (docUrls == null) docUrls = docQuery(args.allQuery, args.queryType, args.fetcherArgs);
			else docUrls.addAll(docQuery(args.allQuery, args.queryType, args.fetcherArgs));
		}

		FetcherUtil.run(args.fetcherUtilArgs, new Fetcher(args.fetcherArgs), publicationIds, webpageUrls, docUrls);

		Util.run(args);
	}
}
