package com.bio.sample.service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bio.sample.dao.PathDao;
import com.bio.sample.dao.SampleDao;
import com.bio.sample.io.FileUtil;

/**
 * Dequeues the discovered files from the Elasticsearch and process the parallely.
 *
 */
@Component
public class FileProcessor {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private PathDao pathDao;

	@Autowired
	private SampleDao sampleDao;

	@Autowired
    private TaskExecutor taskExecutor;
	
	@Scheduled(fixedRate = 1000L * 5)
	private void process() {
		try {
			List<Path> list = pathDao.getPaths();
			if (!list.isEmpty()) {
				logger.info("Process number of files {}", list.size());
			}

			list.forEach(p -> taskExecutor.execute(() -> process(p)));

		} catch (Exception ex) {
			logger.error("File process error", ex);
		}
	}

	private void process(Path path) {
		try {
			if (FileUtil.isSampleFile(path)) {
				sampleDao.createIndex(path);
			}
		} catch (IOException ex) {
			logger.error(String.format("File %s process error", path), ex);
		}
	}
}