package cloud.kpipe.logscanner;

import cloud.kpipe.ui.RunnerPanelController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class LogScanner {

    public static final String PIPELINE_PNG = "pipeline.png";
    public static final String STATUS = "status";
    public static final String COMMAND = "command";
    public static final String SUCCESS = "success";
    public static final String OUTPUT = "output";
    public static final String ERROR = "error";
    public static final String STARTED = "started";
    public static final String TERMINATED = "terminated";
    private Path dir;

    @Inject
    private RunnerPanelController controller;

    private WatchService watchService;

    private Thread thread;

    private boolean running;

    Runnable updateRunnable;
    private Set<Path> watchedDirs = new HashSet<>();

    public LogScanner() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Could not create watch service", e);
        }
    }

    public void setUpdateRunnable(Runnable updateRunnable) {
        this.updateRunnable = updateRunnable;
    }

    public void startWatching(Path dir) {
        if (this.dir != null) {
            if (this.dir != dir) {
                throw new RuntimeException("tried to watch two differnt dirs");
            }
            return;
        }
        System.out.println("Starting watching thread for "+dir);
        this.dir = dir;
        registerDir(dir);
        running = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        WatchKey key = watchService.take(); // Blocks until an event occurs
                        processEvents(key);
                        updateRunnable.run();
                        key.reset(); // Reset the key to receive further events for this directory
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }

    public void stopWatching() {
        System.out.println("Stopping watcher thread");
        running = false;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Watcher thread terminated");
        try {
            watchService.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not close watch service", e);
        }
    }

    private void registerDir(Path parent) {
        try {
            System.out.println("Watching directory "+parent);
            parent.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException e) {
            throw new RuntimeException("Could not register directory "+parent+" for watching", e);
        }
        scanTree(parent);
    }

    private void processEvents(WatchKey key) {
        int num = 0;
        Path watched = (Path) key.watchable();
        System.out.println("Polling events for watch key: "+watched);
        List<WatchEvent<?>> events = key.pollEvents();
        System.out.println("Received "+events.size()+" events to process");
        for (WatchEvent<?> event : events) {
            WatchEvent.Kind<?> kind = event.kind();
            Path affectedFile = watched.resolve((Path) event.context());
            System.out.println("File event: " + kind + ". File: " + affectedFile);
            processEvent(kind, affectedFile);
            num++;
        }
        System.out.println(num+" file events were processed.");
    }

    private void processEvent(WatchEvent.Kind<?> kind, Path file) {
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE) || kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            processPath(file);
        }
    }

    public void scanTree(Path parent) {
        try {
            System.out.println("Scanning directory "+parent);
            Files.walk(parent).forEach(this::processPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not walk file tree "+parent, e);
        }
    }

    public void fullScan() {
        scanTree(dir);
        updateRunnable.run();
    }

    private void processPath(Path path) {
        System.out.println("File found: "+path);
        if (path.toFile().isDirectory()) {
            if (watchedDirs.add(path)) {
                registerDir(path);
            }
        } else {
            System.out.println("processing file: "+path);
            Path relPath = dir.relativize(path);
            processFile(relPath);
        }
    }

    private void processFile(Path relPath) {
        if (relPath.getNameCount() == 1) {
            String filename = relPath.getName(0).toString();
            switch (filename) {
                case PIPELINE_PNG -> setImage(filename);
                case STATUS -> setStatus(filename);
                default -> System.out.println("unknown file: "+relPath);
            }
        } else if (relPath.getNameCount() == 3) {
            String command = relPath.getName(0).toString();
            String step = relPath.getName(1).toString();
            String filename = relPath.getName(2).toString();
            try {
                List<String> lines = Files.readAllLines(dir.resolve(relPath));
                switch (filename) {
                    case COMMAND -> setCommand(command, step, lines);
                    case SUCCESS -> setSuccess(command, step, lines);
                    case OUTPUT -> setOutput(command, step, lines);
                    case ERROR -> setError(command, step, lines);
                    case STARTED -> setStarted(command, step, lines);
                    case TERMINATED -> setTerminated(command, step, lines);
                    default -> System.out.println("unknown file: "+relPath);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read file "+relPath, e);
            }
        } else {
            System.err.println("Name count unexpected: "+relPath);
        }
    }

    private void setStatus(String filename) {
        controller.setStatus(expectOneLine(filename));
    }

    private String expectOneLine(String filename) {
        try {
            List<String> lines = Files.readAllLines(dir.resolve(filename));
            if (lines.size() != 1) {
                throw new RuntimeException("Expected one line in "+filename+", found "+lines.size());
            }
            return lines.get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setImage(String filename) {
        controller.setImage(filename);
    }

    private void setCommand(String command, String step, List<String> lines) {
        if (lines.size() != 1) {
            System.err.println("Got multiple commands: "+lines.size());
        } else {
            controller.setCommandLine(command, step, lines.get(0));
        }
    }

    private void setOutput(String command, String step, List<String> lines) {
        controller.setOutput(command, step, lines);
    }

    private void setError(String command, String step, List<String> lines) {
        controller.setError(command, step, lines);
    }

    private void setSuccess(String command, String step, List<String> lines) {
        if (lines.size() != 1) {
            System.err.println("Got multiple commands: "+lines.size());
        } else {
            boolean success = switch (lines.get(0)) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new RuntimeException("Could not parse boolean from "+lines.get(0));
            };
            controller.setStepSuccess(command, step, success);
        }
    }

    private void setStarted(String command, String step, List<String> lines) {
        if (lines.size() != 1) {
            System.err.println("Got multiple lines for started: "+lines.size());
        } else {
            controller.setStarted(command, step, LocalDateTime.parse(lines.get(0)));
        }
    }

    private void setTerminated(String command, String step, List<String> lines) {
        if (lines.size() != 1) {
            System.err.println("Got multiple lines for started: "+lines.size());
        } else {
            controller.setTerminated(command, step, LocalDateTime.parse(lines.get(0)));
        }
    }

}
