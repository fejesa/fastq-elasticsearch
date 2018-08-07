package com.bio.sample.service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bio.sample.dao.PathDao;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Recursively collects files from the configured folders.
 * Discovered file paths are temporarily stored in the Elasticsearch.
 * Currently on FASTQ files are collected.
 * 
 * Task is scheduled.
 *
 */
@Component
public class FileDiscoverer implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String CONF_APPLICATION = "sampledb.conf";
	
	private static final String CONF_ROOT_FOLDERS = "root.folders";
	
	private static final String CONF_EXCLUSIVE_FOLDERS = "folders.exclusive";

	@Autowired
	private PathDao dao;

	private List<Path> roots;

	private List<Path> exclusiveFolders;

	@Override
	public void afterPropertiesSet() throws Exception {
		
		final Config applicationConfig = ConfigFactory.load(CONF_APPLICATION);
		
		roots = applicationConfig.getStringList(CONF_ROOT_FOLDERS)
				.stream()
				.map(Paths::get)
				.collect(Collectors.toList());
		
		exclusiveFolders = applicationConfig.getStringList(CONF_EXCLUSIVE_FOLDERS)
				.stream()
				.map(Paths::get)
				.collect(Collectors.toList());
		
		logger.info("Configured root folders: {}", roots);
		
		logger.info("Configured exclusive folders: {}", exclusiveFolders);
	}

	@Scheduled(fixedRate = 1000L * 3600 * 12)
	private void discover() throws IOException {
		for (Path root : roots) {
			try {
				logger.info("Start discovering of root {}", root);
				discover(root);
			} catch (IOException e) {
				logger.error(String.format("Root %s discovery error", root), e);
			}
		}
	}

	private void discover(Path root) throws IOException {

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

			final FileSystem fs = root.getFileSystem();
			final PathMatcher matcher = fs.getPathMatcher("glob:*.{fastq.gz}");

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (exclusiveFolders.contains(dir)) {
					return FileVisitResult.SKIP_SIBLINGS;
				}
				try {
					return super.preVisitDirectory(dir, attrs);
				} catch (IOException ex) {
					logger.warn(String.format("Folder %s cannot be parsed", dir), ex);
					return FileVisitResult.SKIP_SIBLINGS;
				}
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// Symlink is ignored
				if (attrs.isRegularFile() && matcher.matches(file.getFileName())) {
					// Found a sample/analysis file, persist it
					logger.info("Found {}, enqueued it", file);
					dao.createIndex(file);
				}

				return super.visitFile(file, attrs);
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
				logger.warn(String.format("File %s cannot be parsed", file), ex);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}