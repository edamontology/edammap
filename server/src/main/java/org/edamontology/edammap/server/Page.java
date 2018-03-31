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

package org.edamontology.edammap.server;

import java.io.IOException;
import java.io.StringWriter;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.output.Params;

public final class Page {

	static String get(CoreArgs args) {
		StringWriter writer = new StringWriter();

		writer.write("<!DOCTYPE html>\n");
		writer.write("<html lang=\"en\">\n\n");

		writer.write("<head>\n");
		writer.write("\t<meta charset=\"utf-8\">\n");
		writer.write("\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		writer.write("\t<meta name=\"author\" content=\"Erik Jaaniso\">\n");
		writer.write("\t<title>" + Server.version.getName() + " " + Server.version.getVersion() + "</title>\n");
		writer.write("\t<link rel=\"stylesheet\" href=\"/" + Server.args.getPath() + "/style.css\">\n");
		writer.write("</head>\n\n");

		writer.write("<body>\n\n");

		writer.write("<form action=\"/" + Server.args.getPath() + "/api\" method=\"post\">\n\n");

		writer.write("<header>\n\n");

		writer.write("<h1>EDAMmap " + Server.version.getVersion() + "</h1>\n\n");

		writer.write("<p><a href=\"https://github.com/edamontology/edammap\">https://github.com/edamontology/edammap</a></p>\n\n");

		writer.write("</header>\n\n");

		writer.write("<main>\n\n");

		writer.write("<article>\n");
		writer.write("\t<section id=\"name-section\">\n");
		writer.write("\t\t<h3>Name</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">Name of tool or service<br><span class=\"ex\">Ex:</span> <span class=\"example\">g:Profiler</span></div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><input type=\"text\" id=\"name\" name=\"name\" required maxlength=\"" + Resource.MAX_NAME_LENGTH + "\"></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("\t<section id=\"keywords-section\">\n");
		writer.write("\t\t<h3>Keywords</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">Keywords, tags, etc. One per line.<br>Lines beginning with '#' are ignored.</div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><textarea id=\"keywords\" name=\"keywords\" rows=\"3\" maxlength=\"" + Resource.MAX_KEYWORDS_LENGTH + "\"></textarea></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("</article>\n\n");

		writer.write("<article>\n");
		writer.write("\t<section id=\"description-section\">\n");
		writer.write("\t\t<h3>Description</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">Short description of tool or service</div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><textarea id=\"description\" name=\"description\" rows=\"3\" maxlength=\"" + Resource.MAX_DESCRIPTION_LENGTH + "\"></textarea></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("</article>\n\n");

		writer.write("<article>\n");
		writer.write("\t<section id=\"webpage-urls-section\">\n");
		writer.write("\t\t<h3>Links</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">URLs of homepage, etc<br><span class=\"ex\">Ex:</span> <span class=\"example\">https://biit.cs.ut.ee/gprofiler/</span></div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><textarea id=\"webpage-urls\" name=\"webpage-urls\" rows=\"3\" onblur=\"check('webpage-urls','PATCH','/" + Server.args.getPath() + "/api/web')\" maxlength=\"" + Resource.MAX_LINKS_LENGTH + "\"></textarea></div>\n");
		writer.write("\t\t\t\t<div id=\"webpage-urls-output\" class=\"output\"></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("\t<section id=\"doc-urls-section\">\n");
		writer.write("\t\t<h3>Documentation</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">URLs of documentations<br><span class=\"ex\">Ex:</span> <span class=\"example\">https://biit.cs.ut.ee/gprofiler/help.cgi</span></div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><textarea id=\"doc-urls\" name=\"doc-urls\" rows=\"3\" onblur=\"check('doc-urls','PATCH','/" + Server.args.getPath() + "/api/doc')\" maxlength=\"" + Resource.MAX_LINKS_LENGTH + "\"></textarea></div>\n");
		writer.write("\t\t\t\t<div id=\"doc-urls-output\" class=\"output\"></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("</article>\n\n");

		writer.write("<article>\n");
		writer.write("\t<section id=\"publication-ids-section\">\n");
		writer.write("\t\t<h3>Publications</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">PMID/PMCID/DOI of journal article<br><span class=\"ex\">Ex:</span> <span class=\"example\">17478515<br>PMC3125778<br>10.1093/nar/gkw199</span></div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><textarea id=\"publication-ids\" name=\"publication-ids\" rows=\"3\" onblur=\"check('publication-ids','PATCH','/" + Server.args.getPath() + "/api/pub')\" maxlength=\"" + Resource.MAX_PUBLICATION_IDS_LENGTH + "\"></textarea></div>\n");
		writer.write("\t\t\t\t<div id=\"publication-ids-output\" class=\"output\"></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("\t<section id=\"annotations-section\">\n");
		writer.write("\t\t<h3>Annotations</h3>\n");
		writer.write("\t\t<div>\n");
		writer.write("\t\t\t<div class=\"label\">Existing annotations from EDAM<br><span class=\"ex\">Ex:</span> <span class=\"example\">http://edamontology.org/topic_1775<br>operation_2436<br>data_3021<br>format_1964</span></div>\n");
		writer.write("\t\t\t<div class=\"io\">\n");
		writer.write("\t\t\t\t<div class=\"input\"><textarea id=\"annotations\" name=\"annotations\" rows=\"5\" onblur=\"check('annotations','POST','/" + Server.args.getPath() + "/api/edam')\" maxlength=\"" + Resource.MAX_ANNOTATIONS_LENGTH + "\"></textarea></div>\n");
		writer.write("\t\t\t\t<div id=\"annotations-output\" class=\"output\"></div>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
		writer.write("\t</section>\n");
		writer.write("</article>\n\n");

		writer.write("<div id=\"map\"><span><input type=\"submit\" value=\"MAP\"></span></div>\n\n");

		writer.write("</main>\n\n");

		writer.write("<footer>\n\n");

		writer.write("<h2>Parameters</h2>\n\n");

		writer.write("<section id=\"tabs\">\n");
		writer.write("\n");
		try {
			Params.writeMain(Server.paramsMain, writer, false);
			Params.writeProcessing(args.getProcessorArgs(), writer);
			Params.writePreProcessing(args.getPreProcessorArgs(), writer, true);
			Params.writeFetching(args.getFetcherArgs(), writer, false, true);
			Params.writeMapping(args.getMapperArgs(), writer, true);
			Params.writeCountsEdamOnly(writer, Server.concepts);
		} catch (IOException e) {
			// TODO
		}
		writer.write("</section>\n\n");

		writer.write("</footer>\n\n");

		writer.write("</form>\n\n");

		writer.write("<script src=\"/" + Server.args.getPath() + "/script.js\"></script>\n\n");

		writer.write("</body>\n\n");

		writer.write("</html>\n");

		return writer.toString();
	}
}
