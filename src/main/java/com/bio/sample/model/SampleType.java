package com.bio.sample.model;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;

import com.bio.sample.io.FileMetaInf;
import com.bio.sample.io.FileMetaUtil;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "sample")
public class SampleType extends Type {

	private String sampleName;
	
	private String samplePath;
	
	private String checksum;
	
	private int readCount;
	
	private long fileLength;
	
	private int readLength;

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getSamplePath() {
		return samplePath;
	}

	public void setSamplePath(String path) {
		this.samplePath = path;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public int getReadCount() {
		return readCount;
	}

	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	public int getReadLength() {
		return readLength;
	}

	public void setReadLength(int readLength) {
		this.readLength = readLength;
	}
	
	public static class SampleTypeBuilder extends Builder<SampleType> {
		
		public SampleType build() throws IOException {
			
			FileMetaInf meta = FileMetaUtil.get(path);

			SampleType sample = new SampleType();
			sample.setSampleName(path.getFileName().toString());
			sample.setChecksum(meta.getChecksum());
			sample.setCreationTime(LocalDateTime.now().format(formatter));
			sample.setReadCount((int)meta.getReadNumber());
			sample.setReadLength(meta.getMaximumReadLength());
	
			try (FileChannel file = FileChannel.open(path);) {
				sample.setFileLength(file.size());
				sample.setSamplePath(path.toAbsolutePath().toString());	
			}

			return sample;
		}
	}
}