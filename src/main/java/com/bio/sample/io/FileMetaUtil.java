package com.bio.sample.io;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileMetaUtil {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String DEFAULT_ALGORITHM = "MD5";

	private static final int MAX_RECORD = 2000;

	public static FileMetaInf get(final Path path) throws IOException {

		try {

			final String fileName = path.toAbsolutePath().toString();

			if (FileUtil.isFastqFile(fileName)) {
				return FileMetaUtil.fastq(path);
			} else {
				throw new IllegalArgumentException("unsupported file type is set: " + path);
			}

		} catch (final NoSuchAlgorithmException nsae) {
			throw new RuntimeException("Digest algorithm not found");
		} catch (final NoSuchElementException nse) {
			throw new IOException(nse);
		}
	}

	private static FileMetaInf fastq(final Path path) throws IOException, NoSuchAlgorithmException {

		final MessageDigest messageDigest = getDigest();

		final Predicate<Path> isCompressed = p -> p.toAbsolutePath().toString().endsWith(".gz");
		final InstumentedFile file = new InstumentedFile(path);

		long chars = 0;
		int counter = 0;
		int maxReadLength = 0;
		try (Stream<String> stream = isCompressed.test(path) ? GZIPFiles.lines(file) : Files.lines(path);) {

			for (final Iterator<String> it = stream.iterator(); it.hasNext()
					&& counter < FileMetaUtil.MAX_RECORD; ++counter) {

				final String id = it.next();
				if (!id.startsWith("@")) {
					throw new IOException("Bad fastq record id in file " + path);
				}
				final String read = it.next();
				maxReadLength = Math.max(maxReadLength, read.length());

				final String s = it.next();
				final String quality = it.next();
				if (quality.length() != read.length()) {
					throw new IOException("Different length of read and quality in file " + path);
				}

				update(messageDigest, id, read, quality);

				chars += id.length() + read.length() + s.length() + quality.length() + 4;
			}
		} catch (final NoSuchElementException nse) {
			logger.warn("File {} contains less read than we expected", path);
		}

		final FileMetaInf metaInf = new FileMetaInf();
		metaInf.setChecksum(bytesToHex(messageDigest.digest()));

		final double total = isCompressed.test(path) ? (double) Files.size(path) / file.getReadBytes() * counter
				: (double) Files.size(path) / chars * counter;
		metaInf.setReadNumber((long) total);

		metaInf.setMaximumReadLength(maxReadLength);
		return metaInf;
	}

	public static MessageDigest getDigest() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(DEFAULT_ALGORITHM);
	}

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(final byte[] bytes) {
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	public static void update(final MessageDigest messageDigest, final String id, final String read,
            final String quality) {
        messageDigest.update(id.getBytes(StandardCharsets.UTF_8));
        messageDigest.update(read.getBytes(StandardCharsets.UTF_8));
        messageDigest.update(quality.getBytes(StandardCharsets.UTF_8));
    }
}