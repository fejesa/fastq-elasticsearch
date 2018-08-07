package com.bio.sample.model;

import java.time.format.DateTimeFormatter;

/**
 * Abstract document type that holds common attributes of the documents.
 *
 */
public abstract class Type {

	protected static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private String creationTime;

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}
}
