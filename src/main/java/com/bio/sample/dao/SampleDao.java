package com.bio.sample.dao;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Repository;

import com.bio.sample.model.SampleType;
import com.bio.sample.model.SampleType.SampleTypeBuilder;

/**
 * Used for creating sample document type.
 *
 */
@Repository
public class SampleDao extends BaseDao {

	public void createIndex(Path path) throws IOException {
		super.createIndex(path, "sample.samplePath.exact");
	}

	protected SampleType createType(Path path) throws IOException {
		return new SampleTypeBuilder().setPath(path).build();
	}
}
