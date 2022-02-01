package hu.codelens.sharecenter.internal;

import hu.codelens.sharecenter.JShareCenterMediaPath;

public class DefaultJShareCenterMediaPath implements JShareCenterMediaPath {

    private final String volume;
    private final String internalPath;

    DefaultJShareCenterMediaPath(String volume, String internalPath) {
        this.volume = volume;
        this.internalPath = internalPath;
    }

    @Override
    public String getVolume() {
        return volume;
    }

    @Override
    public String getInternalPath() {
        return internalPath;
    }
}
