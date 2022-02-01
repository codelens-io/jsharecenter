package hu.codelens.sharecenter;

import hu.codelens.sharecenter.internal.DefaultJShareCenter;

import java.util.Collection;
import java.util.Optional;

public interface JShareCenter {

    static JShareCenter getDefault(String host) {
        return new DefaultJShareCenter(host);
    }

    boolean login(String username, String base64password);

    void logout(String username);

    Optional<Boolean> checkMediaScanRunning();

    Optional<Boolean> startMediaScan(JShareCenterMediaPath mediaPath);

    Optional<Boolean> checkMediaScanFinished();

    Optional<Integer> checkMediaScanProgress();

    Optional<Collection<JShareCenterMediaPath>> getMediaServerPathList();
}
