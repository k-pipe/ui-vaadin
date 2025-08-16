package cloud.kpipe.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ActionHandler {

    @Inject
    RunnerPanelController controller;

    public void run() {
        System.out.println("Run button pressed");
        controller.setStatus("Running");
        controller.addLog("pull", "stepA", "some log");
        controller.addLog("simulate", "stepB", "some other log");
        controller.addLog("simulate", "stepC", "some third log");
        controller.setSelectedCommand("pull");
        controller.setSelectedStep("pull", "stepA");
    }

}
