package cloud.kpipe.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class RunnerPanelController {

    @Inject
    RunnerPanelModel model;

    Runnable updateRunnable;

    public void setUpdateRunnable(Runnable updateRunnable) {
        this.updateRunnable = updateRunnable;
    }

    public void addLog(String command, String step, String logLine) {
        model.commandLogs.putIfAbsent(command, new LogSet());
        LogSet commandLog = model.commandLogs.get(command);
        commandLog.stepLogs.putIfAbsent(step, new StepLog());
        commandLog.stepLogs.get(step).logLines.add(logLine);
        commandLog.selectedStep = step;
        updateRunnable.run();
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
      updateRunnable.run();
    }

    public void setOutput(String command, String step, List<String> lines) {
        model.commandLogs.putIfAbsent(command, new LogSet());
        LogSet commandLog = model.commandLogs.get(command);
        StepLog sl = new StepLog();
        sl.logLines = lines;
        commandLog.stepLogs.put(step, sl);
        commandLog.selectedStep = step;
        updateRunnable.run();
    }

    public void setCommandLine(String command, String step, String commandline) {
    }

    public void setImage(File imageFile) {
    }
}
