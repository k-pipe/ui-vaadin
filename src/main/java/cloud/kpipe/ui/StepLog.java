package cloud.kpipe.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StepLog {
    public List<String> logLines;

    public String command;
    public boolean success;
    public double time;

    public String getSummary() {
        return (success ? "Succeeded in" : "Failed after") + " " + String.format("%f.1",time) +"s";
    }
}
