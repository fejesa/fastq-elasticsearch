package com.bio.sample.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

class GZIPFiles {

	public static class InstrumentedGZIPInputStream extends GZIPInputStream {

		private long counter;

		public InstrumentedGZIPInputStream(InputStream in) throws IOException {
			super(in);
		}

		public InstrumentedGZIPInputStream(InputStream in, int size) throws IOException {
			super(in, size);
		}

		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			final int n = super.read(buf, off, len);
			this.counter = this.inf.getBytesRead();
			return n;
		}

		public long getCounter() {
			return this.counter;
		}
	}

	private static Consumer<Closeable> closeSafely = c -> {
		try {
			if (c != null) {
				c.close();
			}
		} catch (final IOException e) {
			// Ignore
		}
	};

	/**
	 * Get a lazily loaded stream of lines from a gzipped file, similar to
	 * {@link Files#lines(java.nio.file.Path)}.
	 * 
	 * @param path
	 *            The path to the gzipped file.
	 * @return stream with lines.
	 */
	public static Stream<String> lines(InstumentedFile file) {

		InputStream fileIs = null;
		InstrumentedGZIPInputStream gzipIs = null;
		try {
			fileIs = Files.newInputStream(file.getPath());
			// Even though GZIPInputStream has a buffer it reads individual bytes
			// when processing the header, better add a buffer in-between
			gzipIs = new InstrumentedGZIPInputStream(fileIs, 65535);

			final InstrumentedGZIPInputStream gzip = gzipIs;
			file.setReadBytes(gzip::getCounter);

		} catch (final IOException e) {
			GZIPFiles.closeSafely.accept(gzipIs);
			GZIPFiles.closeSafely.accept(fileIs);
			throw new UncheckedIOException(e);
		}
		final BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIs));

		return reader.lines().onClose(() -> GZIPFiles.closeSafely.accept(reader));
	}

	public static Stream<String> lines(Path path) {
		InputStream fileIs = null;
		GZIPInputStream gzipIs = null;
		try {
			fileIs = Files.newInputStream(path);
			gzipIs = new GZIPInputStream(fileIs, 65535);
		} catch (final IOException e) {
			GZIPFiles.closeSafely.accept(gzipIs);
			GZIPFiles.closeSafely.accept(fileIs);
			throw new UncheckedIOException(e);
		}
		final BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIs));
		return reader.lines().onClose(() -> GZIPFiles.closeSafely.accept(reader));
	}
}