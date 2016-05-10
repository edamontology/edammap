package edammapper.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import edammapper.input.xml.Biotools;
import edammapper.query.QueryType;

public class Xml {

	public static List<Input> load(String queryPath, QueryType type) throws IOException, XMLStreamException, FactoryConfigurationError, ParseException {
		if (queryPath == null || !(new File(queryPath).canRead())) {
			throw new FileNotFoundException("Query file does not exist or is not readable!");
		}

		List<Input> inputs = new ArrayList<>();

		XMLStreamReader reader = null;
		try (FileInputStream fis = new FileInputStream(queryPath)) {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);
			reader = factory.createXMLStreamReader(fis, StandardCharsets.UTF_8.name());

			// TODO validate using schema

			Biotools biotools = null;
			String text = null;

			while (reader.hasNext()) {
				switch (reader.next()) {
				case XMLStreamConstants.START_ELEMENT:
					String uri = reader.getAttributeValue(null, "uri");
					switch (reader.getLocalName()) {
					case "resource":
						biotools = new Biotools();
						break;
					case "topic":
						if (uri != null) biotools.addTopic(uri);
						break;
					case "functionName":
						if (uri != null) biotools.addFunctionName(uri);
						break;
					case "dataType":
						if (uri != null) biotools.addDataType(uri);
						break;
					case "dataFormat":
						if (uri != null) biotools.addDataFormat(uri);
						break;
					}
					text = null;
					break;
				case XMLStreamConstants.CHARACTERS:
					text = reader.getText();
					break;
				case XMLStreamConstants.END_ELEMENT:
					switch (reader.getLocalName()) {
					case "resource":
						inputs.add(biotools);
						biotools = null;
						break;
					case "name":
						if (biotools.getName() == null) biotools.setName(text);
						else; //
						break;
					case "homepage":
						if (biotools.getHomepage() == null) biotools.setHomepage(text);
						else; //
						break;
					case "mirror":
						biotools.addMirror(text);
						break;
					case "description":
						if (biotools.getDescription() == null) biotools.setDescription(text);
						else; //
						break;
					case "docsHome":
						if (biotools.getDocsHome() == null) biotools.setDocsHome(text);
						else; //
						break;
					case "docsGithub":
						if (biotools.getDocsGithub() == null) biotools.setDocsGithub(text);
						else; //
						break;
					case "publicationsPrimaryID":
						if (biotools.getPublicationsPrimaryID() == null) biotools.setPublicationsPrimaryID(text);
						else; //
						break;
					case "publicationsOtherID":
						biotools.addPublicationsOtherID(text);
						break;
					}
					break;
				}
			}
			// TODO catch NullPointer and throw again with record number i
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		int i = 0;
		for (Input input : inputs) {
			input.check(++i);
		}

		return inputs;
	}
}
