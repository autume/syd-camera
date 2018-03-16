package com.oden.syd_camera.entity;

import java.util.Date;

/**
 * Created by syd on 2017/8/21.
 */

public class FileInfo {
    public String name;
    public String path;
    public long lastModified;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name=" + name +
                ", path=" + path +
                ", lastModified=" + lastModified +
                ", lastModified=" + new Date(lastModified).toGMTString() +
                '}';
    }
}
