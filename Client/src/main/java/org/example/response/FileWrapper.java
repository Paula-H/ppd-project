package org.example.response;

import java.io.Serializable;

public class FileWrapper implements Serializable {
    private final String fileName;
    private final byte[] fileData;

    public FileWrapper(String fileName, byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
