/*
 * Copyright © 2018, 2019 Erik Jaaniso
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.FetcherArgs;

public final class ParamParse {

	private static final Logger logger = LogManager.getLogger();

	private static <E extends Enum<E>> E getEnum(String key, List<String> values, Class<E> enumClass, boolean json) {
		try {
			return Enum.valueOf(enumClass, values.get(values.size() - 1));
		} catch (IllegalArgumentException e) {
			throw new ParamException(key, values.get(values.size() - 1), "has invalid value", json);
		}
	}
	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> void paramEnum(String key, List<String> values, Arg<?, ?> arg, Class<E> enumClass, boolean json) {
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			((Arg<E, ?>) arg).setValue(getEnum(key, values, enumClass, json));
		}
	}
	static <E extends Enum<E>> E getParamEnum(MultivaluedMap<String, String> params, String key, Class<E> enumClass, boolean json) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			return getEnum(key, values, enumClass, json);
		} else {
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> void paramEnums(String key, List<String> values, Arg<?, ?> arg, Class<E> enumClass, boolean json) {
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			List<E> enums = new ArrayList<>();
			for (String value : values) {
				try {
					enums.add(Enum.valueOf(enumClass, value));
				} catch (IllegalArgumentException e) {
					throw new ParamException(key, value, "has invalid value", json);
				}
			}
			((Arg<List<E>, ?>) arg).setValue(enums);
		}
	}

	private static Boolean getBoolean(List<String> values) {
		if (values.get(values.size() - 1).isEmpty()) {
			return true;
		} else {
			return Boolean.valueOf(values.get(values.size() - 1));
		}
	}
	private static void paramBoolean(String key, List<String> values, Arg<Boolean, ?> arg) {
		if (values != null && values.size() > 0) {
			arg.setValue(getBoolean(values));
		}
	}
	static Boolean getParamBoolean(MultivaluedMap<String, String> params, String key) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0) {
			return getBoolean(values);
		} else {
			return null;
		}
	}

	private static void paramInteger(String key, List<String> values, Arg<Integer, ?> arg, boolean json) {
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			Integer value;
			try {
				value = Integer.valueOf(values.get(values.size() - 1));
			} catch (NumberFormatException e) {
				throw new ParamException(key, values.get(values.size() - 1), "has wrong number format", json);
			}
			if (arg.getMin() != null && value < arg.getMin()) {
				throw new ParamException(key, value.toString(), "is below limit " + arg.getMin(), json);
			}
			if (arg.getMax() != null && value > arg.getMax()) {
				throw new ParamException(key, value.toString(), "is above limit " + arg.getMax(), json);
			}
			arg.setValue(value);
		}
	}

	private static void paramDouble(String key, List<String> values, Arg<Double, ?> arg, boolean json) {
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			Double value;
			try {
				value = Double.valueOf(values.get(values.size() - 1));
			} catch (NumberFormatException e) {
				throw new ParamException(key, values.get(values.size() - 1), "has wrong number format", json);
			}
			if (arg.getMin() != null && value < arg.getMin()) {
				throw new ParamException(key, value.toString(), "is below limit " + arg.getMin(), json);
			}
			if (arg.getMax() != null && value > arg.getMax()) {
				throw new ParamException(key, value.toString(), "is above limit " + arg.getMax(), json);
			}
			arg.setValue(value);
		}
	}

	private static void paramString(String key, List<String> values, Arg<String, ?> arg) {
		if (values != null && values.size() > 0) {
			arg.setValue(values.get(values.size() - 1));
		}
	}
	static String getParamString(MultivaluedMap<String, String> params, String key) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0) {
			return values.get(values.size() - 1);
		} else {
			return null;
		}
	}
	static String getParamStrings(MultivaluedMap<String, String> params, String key) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0) {
			return String.join("\n", values);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static void param(MultivaluedMap<String, String> params, Arg<?, ?> arg, boolean json) {
		for (Map.Entry<String, List<String>> paramEntry : params.entrySet()) {
			if (paramEntry.getKey().equals(arg.getId())) {
				if (arg.getEnumClass() != null) {
					if (arg.getValue() instanceof List) {
						paramEnums(paramEntry.getKey(), paramEntry.getValue(), arg, arg.getEnumClass(), json);
					} else {
						paramEnum(paramEntry.getKey(), paramEntry.getValue(), arg, arg.getEnumClass(), json);
					}
				} else if (arg.getValue() instanceof Boolean) {
					paramBoolean(paramEntry.getKey(), paramEntry.getValue(), (Arg<Boolean, ?>) arg);
				} else if (arg.getValue() instanceof Integer) {
					paramInteger(paramEntry.getKey(), paramEntry.getValue(), (Arg<Integer, ?>) arg, json);
				} else if (arg.getValue() instanceof Double) {
					paramDouble(paramEntry.getKey(), paramEntry.getValue(), (Arg<Double, ?>) arg, json);
				} else if (arg.getValue() instanceof String) {
					paramString(paramEntry.getKey(), paramEntry.getValue(), (Arg<String, ?>) arg);
				} else {
					throw new IllegalArgumentException("Param with id " + arg.getId() + " is of illegal class " + arg.getValue().getClass().getName() + "!");
				}
			}
		}
	}

	static void parseFetcherParams(MultivaluedMap<String, String> params, FetcherArgs fetcherArgs, boolean json) {
		if (params == null || params.isEmpty()) {
			if (params == null) {
				logger.warn("No params to parse");
			}
			return;
		}

		for (Arg<?, ?> arg : fetcherArgs.getArgs()) {
			param(params, arg, json);
		}
	}

	static void parseParams(MultivaluedMap<String, String> params, CoreArgs args, boolean json) {
		if (params == null || params.isEmpty()) {
			if (params == null) {
				logger.warn("No params to parse");
			}
			return;
		}

		for (Arg<?, ?> arg : args.getPreProcessorArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getFetcherArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getAlgorithmArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getIdfArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getMultiplierArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getNormaliserArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getWeightArgs().getArgs()) {
			param(params, arg, json);
		}
		for (Arg<?, ?> arg : args.getMapperArgs().getScoreArgs().getArgs()) {
			param(params, arg, json);
		}
	}
}
