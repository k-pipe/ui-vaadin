package cloud.kpipe.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class RunnerPanelController {

    @Inject
    RunnerPanelModel model;

    public void addLog(String command, String step, String logLine) {
        model.commandLogs.putIfAbsent(command, new LogSet());
        LogSet commandLog = model.commandLogs.get(command);
        commandLog.stepLogs.putIfAbsent(step, new StepLog());
        commandLog.stepLogs.get(step).logLines.add(logLine);
        commandLog.selectedStep = step;
    }

    public void setSelectedCommand(String command) {
        model.selectedCommand = command;
    }

    public void setSelectedStep(String command, String step) {
        LogSet cl = model.commandLogs.get(command);
        if (cl != null) {
            setSelectedCommand(command);
            cl.selectedStep = step;
        }
    }

    public void setStatus(String text) {
      model.statusLine = text;
    }

    private LogSet getOrCreateLogSet(String command) {
        model.commandLogs.putIfAbsent(command, new LogSet());
        return model.commandLogs.get(command);
    }

    private StepLog getOrCreateStepLog(String command, String step) {
        LogSet logset = getOrCreateLogSet(command);
        logset.stepLogs.putIfAbsent(step, new StepLog());
        return logset.stepLogs.get(step);
    }

    public void setOutput(String command, String step, List<String> lines) {
        getOrCreateStepLog(command, step).logLines = lines;
    }
    public void setError(String command, String step, List<String> lines) {
        getOrCreateStepLog(command, step).errorLines = lines;
    }

    public void setCommandLine(String command, String step, String commandline) {
        getOrCreateStepLog(command, step).commandLine = commandline;
    }

    public void setImage(String imageFile) {
        System.out.println("Updated image file: "+imageFile);
        model.imageFile = imageFile;
        model.imageWasUpdated = true;
    }

    public void setStepSuccess(String command, String step, boolean success) {
        getOrCreateStepLog(command, step).success = success;
    }

    public void setStarted(String command, String step, LocalDateTime timestamp) {
        getOrCreateStepLog(command, step).started = timestamp;
    }

    public void setTerminated(String command, String step, LocalDateTime timestamp) {
        getOrCreateStepLog(command, step).terminated = timestamp;
    }

}
