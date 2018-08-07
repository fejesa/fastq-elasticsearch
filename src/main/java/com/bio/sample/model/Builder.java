package com.bio.sample.model;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract document type builder.
 *
 * @param <T> The supported document type.
 */
public abstract class Builder<T extends Type> {

	/** Reference to the analysis or sample file. */
	protected Path path;

	public Builder<T> setPath(Path path) {
		this.path = path;
		return this;
	}
	
	/**
	 * Creates the document type about the given file.
	 * @return
	 * @throws IOException
	 */
	public abstract T build() throws IOException;
	
}
