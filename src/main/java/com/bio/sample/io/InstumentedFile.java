package com.bio.sample.io;

import java.nio.file.Path;
import java.util.function.LongSupplier;

final class InstumentedFile {

	private final Path path;

	private LongSupplier readBytes;

	public InstumentedFile(Path path) {
		this.path = path;
	}

	public Path getPath() {
		return this.path;
	}

	public void setReadBytes(LongSupplier readBytes) {
		this.readBytes = readBytes;
	}

	public long getReadBytes() {
		if (this.readBytes == null) {
			return -1;
		}
		return this.readBytes.getAsLong();
	}
}