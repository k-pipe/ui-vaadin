package cloud.kpipe.logscanner;

import cloud.kpipe.ui.RunnerPanelController;
import cloud.kpipe.ui.RunnerPanelModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Singleton
public class LogScanner {

    private Path dir;

    @Inject
    private RunnerPanelController controller;

    private WatchService watchService;

    private Thread thread;

    private boolean running;

    public LogScanner() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Could not create watch service", e);
        }
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

    private void registerDir(Path dir) {
        try {
            System.out.println("Watching directory "+dir);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException e) {
            throw new RuntimeException("Could not register directory "+dir+" for watching", e);
        }
    }

    private void processEvents(WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            Path affectedFile = (Path) event.context();
            System.out.println("File event: " + kind + ". File: " + affectedFile);
            processEvent(kind, affectedFile);
        }
    }

    private void processEvent(WatchEvent.Kind<?> kind, Path file) {
    }

    public void fullScan() {
        try {
            Files.walk(dir).forEach(this::processPath);
        } catch (IOException e) {
            throw new RuntimeException("Could nat walk file tree", e);
        }

    }

    private void processPath(Path path) {
        System.out.println("File found: "+path);
        if (path.toFile().isDirectory()) {
            registerDir(path);
        } else {
            processFile(path);
        }
    }

    private void processFile(Path path) {
        Path relPath = dir.relativize(path);
        if (relPath.getNameCount() != 3) {
            System.err.println("Name count unexpected: "+relPath);
        }
        String command = relPath.getName(0).toString();
        String step = relPath.getName(1).toString();
        String filename = relPath.getName(2).toString();
        try {
            List<String> lines = Files.readAllLines(path);
            switch (filename) {
                case "command" -> setCommand(command, step, lines);
                case "result" -> setResult(command, step, lines);
                case "output" -> setOutput(command, step, lines);
                case "error" -> setError(command, step, lines);
                default -> System.out.println("unknown file: "+path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read file "+path, e);
        }
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
    }

    private void setResult(String command, String step, List<String> lines) {
    }


}
