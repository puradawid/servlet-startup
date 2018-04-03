package io.github.puradawid.aem.startup.terminal;

import java.io.File;

class ZipPackage {

    private final String path;

    public ZipPackage(String path) {
        this.path = path;
    }

    boolean validate() {
        return new File(path).exists();
    }

    public File file() {
        return new File(path);
    }

    public String name() {
        return new File(path).getName();
    }
}