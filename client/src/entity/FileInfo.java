package entity;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = -6743567631108323096L;
    private String filename;
    private long time;
    private long size;

    public String getFilename() {
        return filename;
    }

    public long getTime() {
        return time;
    }

    public long getSize() {
        return size;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSize(long size) {
        this.size = size;
    }

}
