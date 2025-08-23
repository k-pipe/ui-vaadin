package cloud.kpipe.ui;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StepLog {
    public List<String> logLines;
    public List<String> errorLines;

    public String commandLine;
    public Boolean success;
    public LocalDateTime started;
    public LocalDateTime terminated;

    public String getSummary() {
        if (started == null) {
            return "Not started yet";
        }
        if (terminated != null) {
            long milis = Duration.between(started, terminated).toMillis();
            double seconds = milis/1000.0;
            double minutes = seconds/60.0;
            String time = minutes > 1.0 ? String.format(Locale.US,"%.1f", minutes) + " minutes" : String.format(Locale.US,"%.1f", seconds) + " seconds";
            return (success == null ? "Terminated after " : success ? "Succeeded in " : "Failed after ") + time;
        }
        return "Started at " + DateTimeFormatter.ofPattern("HH:mm:ss").format(started);
    }


}
