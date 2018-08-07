package com.bio.sample.io;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class FileUtil {

	private static final List<String> FASTQ_FILE_EXTENSIONS = Arrays.asList(".fastq.gz");

	/**
	 * Test if filename refers to one of the supported sample files.
	 */
	public static boolean isFastqFile(final String filename) {
		if (!StringUtils.isBlank(filename)) {
			final String name = filename.toLowerCase();
			return FASTQ_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
		}

		return false;
	}

	/**
	 * Test if filename refers to one of the supported sample files.
	 */
	public static boolean isSampleFile(final Path path) {
		if (path.getFileName() != null) {
			final String filename = path.getFileName().toString();
			return FileUtil.isFastqFile(filename);
		}

		return false;
	}
}