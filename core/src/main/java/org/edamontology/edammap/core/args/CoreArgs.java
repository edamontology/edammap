/*
 * Copyright © 2016, 2018 Erik Jaaniso
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

package org.edamontology.edammap.core.args;

import com.beust.jcommander.ParametersDelegate;

import org.edamontology.pubfetcher.core.common.FetcherArgs;

import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.processing.ProcessorArgs;

public class CoreArgs {

	@ParametersDelegate
	private ProcessorArgs processorArgs = new ProcessorArgs();

	@ParametersDelegate
	private PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

	@ParametersDelegate
	private FetcherArgs fetcherArgs = new FetcherArgs();

	@ParametersDelegate
	private MapperArgs mapperArgs = new MapperArgs();

	public ProcessorArgs getProcessorArgs() {
		return processorArgs;
	}
	public void setProcessorArgs(ProcessorArgs processorArgs) {
		this.processorArgs = processorArgs;
	}

	public PreProcessorArgs getPreProcessorArgs() {
		return preProcessorArgs;
	}
	public void setPreProcessorArgs(PreProcessorArgs preProcessorArgs) {
		this.preProcessorArgs = preProcessorArgs;
	}

	public FetcherArgs getFetcherArgs() {
		return fetcherArgs;
	}
	public void setFetcherArgs(FetcherArgs fetcherArgs) {
		this.fetcherArgs = fetcherArgs;
	}

	public MapperArgs getMapperArgs() {
		return mapperArgs;
	}
	public void setMapperArgs(MapperArgs mapperArgs) {
		this.mapperArgs = mapperArgs;
	}
}
