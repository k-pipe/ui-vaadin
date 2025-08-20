package cloud.kpipe.ui;

import jakarta.inject.Singleton;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class RunnerPanelModel {

    public final Map<String, LogSet> commandLogs = new LinkedHashMap<>();
    public String selectedCommand;

    public String pipelineTitel;
    public String statusLine;

    public boolean imageWasUpdated;
    public String imageFile;

    public boolean requiresUpdateSelectedCommand(String command) {
        return (((command == null) && (selectedCommand != null))
                || (command != null) && !command.equals(selectedCommand));
    }

    public boolean requiresUpdateSelectedStep(String step) {
        if (selectedCommand == null) {
            return false;
        }
        LogSet ls = commandLogs.get(selectedCommand);
        if (ls == null) {
            return false;
        }
        return (((step == null) && (ls.selectedStep != null))
                || (step != null) && !step.equals(ls.selectedStep));
    }

    public boolean wasImageUpdated() {
        return imageWasUpdated;
    }

    public String getImageFile() {
        this.imageWasUpdated = false;
        return imageFile;
    }


}
