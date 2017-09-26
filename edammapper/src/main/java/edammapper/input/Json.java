package edammapper.input;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edammapper.input.json.Biotools;
import edammapper.query.QueryType;

public class Json {

	public static List<? extends InputType> load(String queryPath, QueryType type) throws IOException, ParseException {
		List<? extends InputType> inputs = new ArrayList<>();

		Input input = new Input(queryPath, true);
		try (InputStream is = input.newInputStream()) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

			switch (type) {
				case biotools: inputs = mapper.readValue(input.newInputStream(), Biotools.class).getList(); break;
				default: break;
			}
		}

		int i = 0;
		for (InputType inputType : inputs) {
			inputType.check(++i);
		}

		return inputs;
	}
}
