package com.bio.sample.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "file")
public class FileType extends Type {

	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static class FileTypeBuilder extends Builder<FileType> {

		public FileType build() {
			FileType type = new FileType();
			type.setCreationTime(LocalDateTime.now().format(formatter));
			type.setPath(path.toAbsolutePath().toString());
			return type;
		}
	}
}