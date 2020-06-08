package com.justice.studentfileshareapp;

import java.util.Date;

public class FileData {
    private String fileUrl;
    private String fileName;
    private Date uploadDate;

    public FileData() {
    }

    public FileData(String fileUrl, String fileName, Date uploadDate) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.uploadDate = uploadDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }
}
