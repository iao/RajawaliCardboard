package com.eje_c.rajawalicardboard;

import java.io.File;

public class PanoSet {
    public String name;
    public String url;
    public boolean downloaded = false;
    public File file;
    public long mtime;

    public String getName () {
        return name;
    }

    public PanoSet (File file) {
        this.file = file;
        this.name = file.getName();
        this.downloaded = true;
    }

    public PanoSet (String url, String name, long mtime) {
        this.url = url;
        this.name = name;
        this.mtime = mtime;
    }

    public boolean equals(Object o) {
        if(o instanceof PanoSet) {
            return this.name.equals(((PanoSet) o).name);
        }
        return false;
    }

    public void Combine(PanoSet panoSet) {
        this.url = panoSet.url;
        this.mtime = panoSet.mtime;
    }
}
