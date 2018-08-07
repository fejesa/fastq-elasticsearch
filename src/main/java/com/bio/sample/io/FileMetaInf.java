package com.bio.sample.io;

public final class FileMetaInf {
    
    private long readNumber;
    
    private int maximumReadLength;
    
    private String checksum;
    
    public long getReadNumber() {
        return this.readNumber;
    }
    
    public void setReadNumber(long totalRecord) {
        this.readNumber = totalRecord;
    }
    
    public int getMaximumReadLength() {
        return this.maximumReadLength;
    }
    
    public void setMaximumReadLength(int maximumReadLength) {
        this.maximumReadLength = maximumReadLength;
    }
    
    public String getChecksum() {
        return this.checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
