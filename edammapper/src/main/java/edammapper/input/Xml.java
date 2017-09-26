package edammapper.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import edammapper.input.xml.Biotools14;
import edammapper.query.QueryType;

public class Xml {

	public static List<InputType> load(String queryPath, QueryType type) throws IOException, ParseException {
		List<InputType> inputs = new ArrayList<>();

		Input input = new Input(queryPath, true);

		if (type == QueryType.biotools14) {
			XMLStreamReader reader = null;
			try (InputStream is = input.newInputStream()) {
				XMLInputFactory factory = XMLInputFactory.newInstance();
				factory.setProperty("javax.xml.stream.isCoalescing", true);
				reader = factory.createXMLStreamReader(is, StandardCharsets.UTF_8.name());

				Biotools14 biotools = null;
				String text = null;

				while (reader.hasNext()) {
					switch (reader.next()) {
					case XMLStreamConstants.START_ELEMENT:
						String uri = reader.getAttributeValue(null, "uri");
						switch (reader.getLocalName()) {
							case "resource": biotools = new Biotools14(); break;
							case "topic": if (uri != null) biotools.addTopic(uri); break;
							case "functionName": if (uri != null) biotools.addFunctionName(uri); break;
							case "dataType": if (uri != null) biotools.addDataType(uri); break;
							case "dataFormat": if (uri != null) biotools.addDataFormat(uri); break;
						}
						text = null;
						break;
					case XMLStreamConstants.CHARACTERS:
						text = reader.getText();
						break;
					case XMLStreamConstants.END_ELEMENT:
						switch (reader.getLocalName()) {
							case "resource": inputs.add(biotools); biotools = null;	break;
							case "name": if (biotools.getName() == null) biotools.setName(text); break;
							case "homepage": if (biotools.getHomepage() == null) biotools.setHomepage(text); break;
							case "mirror": biotools.addMirror(text); break;
							case "description": if (biotools.getDescription() == null) biotools.setDescription(text); break;
							case "docsHome": if (biotools.getDocsHome() == null) biotools.setDocsHome(text); break;
							case "docsGithub": if (biotools.getDocsGithub() == null) biotools.setDocsGithub(text); break;
							case "publicationsPrimaryID": if (biotools.getPublicationsPrimaryID() == null) biotools.setPublicationsPrimaryID(text); break;
							case "publicationsOtherID": biotools.addPublicationsOtherID(text); break;
						}
						break;
					}
				}
			} catch (XMLStreamException e) {
				throw new ParseException(e.getLocalizedMessage(), e.getLocation().getLineNumber());
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (XMLStreamException e) {
						throw new ParseException(e.getLocalizedMessage(), e.getLocation().getLineNumber());
					}
				}
			}
		}

		int i = 0;
		for (InputType inputType : inputs) {
			inputType.check(++i);
		}

		return inputs;
	}
}
