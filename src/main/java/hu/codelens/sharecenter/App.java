package hu.codelens.sharecenter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class App {

    private static final String DEFAULT_USERNAME = "admin";
    private final ColorOutput output;

    private String host;
    private String pwd;
    private String username;

    public static void main(String[] args) {
        App app = new App();
        app.processArguments(args);
        app.verifyAndReadMissingArguments();
        app.run();
    }

    public App() {
        this.output = ColorOutput.getDefault();

        host = null;
        pwd = null;
        username = null;
    }

    private void run() {
        JShareCenter shareCenter = JShareCenter.getDefault(host);
        login(shareCenter);

        searchMedia(shareCenter, mediaPaths -> {
            checkMediaScanRunning(shareCenter);
            reindexAllMedia(shareCenter, mediaPaths);
        });

        logout(shareCenter);
    }

    private void reindexAllMedia(JShareCenter shareCenter, Collection<JShareCenterMediaPath> mediaPaths) {
        output.dotLineLn("Reindexing found media");
        mediaPaths.forEach(mediaPath -> {
            output.getOutput().println("  " + mediaPath.getVolume());
            shareCenter.startMediaScan(mediaPath).ifPresentOrElse(scanStarted -> {
                if (scanStarted) {
                    waitMediaScanWithProgressBar(shareCenter);
                } else {
                    output.withYellowLn("Could not start media scan for: " + mediaPath.getVolume());
                }
            }, () -> output.withRedLn("Could not start media scan for: " + mediaPath.getVolume()));
        });
    }

    private void waitMediaScanWithProgressBar(JShareCenter shareCenter) {
        sleep();
        AtomicBoolean running = new AtomicBoolean(true);
        while (running.get()) {
            shareCenter.checkMediaScanProgress().ifPresent(percentage -> {
                output.progress(2, 50, percentage, true);
                sleep();
                if (percentage > 90) {
                    shareCenter.checkMediaScanFinished().ifPresent(finished -> {
                        if (finished) {
                            running.set(false);
                            output.progress(2, 50, 100);
                            output.nl();
                        }
                    });
                }
            });
        }
    }

    private void checkMediaScanRunning(JShareCenter shareCenter) {
        output.dotLine("Checking if pre-scan is already running... ");
        Optional<Boolean> scanState = shareCenter.checkMediaScanRunning();
        if (scanState.isEmpty()) {
            output.withRedLn("could not check state");
            System.exit(-1);
        } else {
            Boolean state = scanState.get();
            if (state) {
                output.withYellowLn("already running");
                System.exit(1);
            } else {
                output.withGreenLn("not running");
            }
        }
    }

    private void logout(JShareCenter shareCenter) {
        output.dotLine("Logging out... ");
        shareCenter.logout(username);
        output.withGreenLn("done");
        output.nl();
    }

    private void login(JShareCenter shareCenter) {
        output.dotLine("Logging in... ");
        if (!shareCenter.login(username, pwd)) {
            output.withRedLn("Login failed");
            System.exit(127);
        }
        output.withGreenLn("logged in");
    }

    private void verifyAndReadMissingArguments() {
        if (host == null) {
            output.dot();
            output.withRedLn(" Host must be specified (without the protocol, just the hostname)");
            output.nl();
            System.exit(2);
        }

        output.dotLineLn("Connecting to ShareCenter: " + host);

        if (username == null) {
            username = DEFAULT_USERNAME;
            output.dotLine("Using default username: ");
            output.withYellowLn(DEFAULT_USERNAME);
        }

        if (pwd == null) {
            char[] password = System.console().readPassword(output.getDot() + " Enter password: ");
            if (password.length > 1) {
                pwd = Base64.getEncoder().encodeToString(new String(password).getBytes());
            } else {
                output.dot();
                output.withRedLn(" Invalid password");
                System.exit(3);
            }
        }
    }

    private void searchMedia(JShareCenter shareCenter, Consumer<Collection<JShareCenterMediaPath>> processMedia) {
        output.dotLine("Searching for media... ");
        Optional<Collection<JShareCenterMediaPath>> foundPathList = shareCenter.getMediaServerPathList();

        if (foundPathList.isEmpty()) {
            output.withYellowLn("not found");
        } else {
            output.withGreenLn("found");
            output.dotLineLn("Listing media to reindex:");
            Collection<JShareCenterMediaPath> mediaPaths = foundPathList.get();
            mediaPaths.forEach(mediaPath -> output.getOutput().println("  " + mediaPath.getVolume()));

            processMedia.accept(mediaPaths);
        }
    }

    private void processArguments(String[] args) {
        Iterator<String> argIterator = Arrays.stream(args).iterator();
        while (argIterator.hasNext()) {
            switch (argIterator.next()) {
                case "-h":
                case "-host":
                case "--h":
                case "--host":
                    if (argIterator.hasNext()) {
                        host = argIterator.next();
                    } else {
                        output.withRedLn("host argument requires a parameter");
                    }
                    break;
                case "-u":
                case "-user":
                case "--user":
                case "--u":
                    if (argIterator.hasNext()) {
                        username = argIterator.next();
                    } else {
                        output.withRedLn("user argument requires a parameter");
                    }
                    break;
                case "-p":
                case "-pass":
                case "--pass":
                case "--p":
                    if (argIterator.hasNext()) {
                        pwd = argIterator.next();
                    } else {
                        output.withRedLn("password argument requires a parameter (base64 encoded password)");
                    }
                    break;
            }
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignored
        }
    }

}
