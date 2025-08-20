package cloud.kpipe.ui;

import cloud.kpipe.logscanner.Constants;
import cloud.kpipe.logscanner.LogScanner;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class RunnerPanelView extends VerticalLayout {

    private static final Path WATCHDIR = Path.of(Constants.LOGDIR);
    private final int TOP_HEIGHT = 60;
    private final int BOTTOM_HEIGHT = 40;
    private final HorizontalLayout top;
    private final VerticalLayout left;
    private final VerticalLayout right;
    private final HorizontalLayout bottom;

    private final Button runButton;

    private final Div statusLine;

    private final Tabs tabs1;
    private final Tabs tabs2;

    private final ConsoleView console;

    @Inject
    RunnerPanelModel model;

    @Inject
    ActionHandler actionHandler;

    @Inject
    LogScanner logscanner;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    UI ui;

    public RunnerPanelView() {
        ui = UI.getCurrent();
        setSpacing(false);
        setPadding(false);

        top = new HorizontalLayout();
        bottom = new HorizontalLayout();
        left = new VerticalLayout();
        right = new VerticalLayout();
        LeftRightPanel leftRight = new LeftRightPanel(left, right);
        add(top, leftRight, bottom);

        top.setWidthFull();
        leftRight.setWidthFull();
        bottom.setWidthFull();

        top.setHeight(TOP_HEIGHT, Unit.POINTS);
        bottom.setHeight(BOTTOM_HEIGHT, Unit.POINTS);
        setFlexGrow(0, top);
        setFlexGrow(1, leftRight);
        setFlexGrow(0, bottom);

        leftRight.setHeight(null);
        left.setHeightFull();
        right.setHeightFull();
        leftRight.setHeightFull();

/*        Div l = new Div();
        l.setText("Left side");
        l.getStyle().set("background-color", "lightblue");
        l.setSizeFull();*/
        tabs1 = new Tabs();
        tabs1.setWidthFull();
        tabs1.addSelectedChangeListener(event -> {
            String selected = event.getSelectedTab() == null ? null : event.getSelectedTab().getLabel();
            if (model.requiresUpdateSelectedCommand(selected)) {
                model.selectedCommand = selected;
               updateTab1Content();
            }
        });
        tabs2 = new Tabs();
        tabs2.setWidthFull();
        tabs2.addSelectedChangeListener(event -> {
            String selected = event.getSelectedTab() == null ? null : event.getSelectedTab().getLabel();
            if (model.requiresUpdateSelectedStep(selected)) {
                model.commandLogs.get(model.selectedCommand).selectedStep = selected;
                updateConsole();
            }
        });
        console = new ConsoleView();
        left.add(tabs1, tabs2, console);
        left.setSizeFull();
        console.setSizeFull();

        /*Div r = new Div();
        r.setText("Right side");
        r.getStyle().set("background-color", "lightgray");*/
        //Image i = getImage();
        //right.add(i);
        //r.setSizeFull();
        //r.add(i);
        //i.setSizeFull();

        Div t = new Div();
        t.setText("Top");
        t.getStyle().set("background-color", "red");
        t.setSizeFull();
        top.add(t);

        statusLine = new Div();
        statusLine.setText("Status");
        bottom.getStyle().set("background-color", "lightgray");
        statusLine.setSizeFull();
        bottom.add(statusLine);
        bottom.setPadding(true);

        runButton = new Button("Run", e -> {
            actionHandler.run();
        });

        // Theme variants give you predefined extra styles for components.
        // Example: Primary button is more prominent look.
        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // You can specify keyboard shortcuts for buttons.
        // Example: Pressing enter in this view clicks the Button.
        runButton.addClickShortcut(Key.ENTER);

        // Use custom CSS classes to apply styling. This is defined in shared-styles.css.
        //addClassName("centered-content");
        top.add(runButton);

        setSizeFull();
    }

    @PostConstruct
    private void init() {
        logscanner.setUpdateRunnable(this::update);
        System.out.println("Reading logs");
        logscanner.startWatching(WATCHDIR);
        logscanner.fullScan();
    }

    @PreDestroy
    private void terminate() {
        System.out.println("Stopping log scanner");
        logscanner.stopWatching();
    }

    public Image getImage(String imageFile) {
        System.out.println("Getting image: "+imageFile);
        // Image
        StreamResource resource = new StreamResource(imageFile, () -> {
            try {
                return new FileInputStream(WATCHDIR.resolve(imageFile).toFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        });
        Image image = new Image(resource, "PlantUML pipeline flow");
        image.setWidthFull();
        image.setHeightFull();
        image.getStyle().set("object-fit", "contain");
        image.getStyle().set("flex-shrink", "1"); // shrink if needed
        image.getStyle().set("flex-grow", "1"); // grow if needed
        return image;
    }

    public void update() {
        if (ui.isAttached()) {
            System.out.println("Triggering update");
            executor.submit(() -> {
                System.out.println("Requesting UI access");
                // background work
                Future<Void> res = ui.access(() -> {
                    System.out.println("Got UI access");
                    // safe UI update inside session lock
                    doUpdate();
                });
                System.out.println("Requested UI access");
                try {
                    Void unused = res.get();
                    System.out.println("Future was obtained");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            System.out.println("UI is not attached any longer");
        }
    }

    private void doUpdate() {
        System.out.println("Running view update");
        statusLine.setText(model.statusLine);
        tabs1.removeAll();
        model.commandLogs.keySet().forEach(this::updateTabTitle1);
        updateTab1Content();
        if (model.wasImageUpdated()) {
            System.out.println("Updating image in right panel");
            Image i = getImage(model.getImageFile());
            right.removeAll();
            right.add(i);
        }
    }

    private void updateTabTitle1(String command) {
        Tab tab = new Tab(command);
        tabs1.add(tab);
    }

    private void updateTab1Content() {
        System.out.println("Log2: "+model.selectedCommand);
        tabs2.removeAll();
        if (model.selectedCommand != null) {
            LogSet logSet = model.commandLogs.get(model.selectedCommand);
            System.out.println("Log3: "+model.selectedCommand);
            logSet.stepLogs.keySet().forEach(this::updateTabTitle2);
            System.out.println("Updated stepLogs: "+logSet.stepLogs.keySet());
        }
        updateConsole();
    }

    private void updateTabTitle2(String step) {
        Tab tab = new Tab(step);
        tabs2.add(tab);
    }

    private void updateConsole() {
        StepLog sl = null;
        if ((model.selectedCommand != null)) {
            LogSet commandLogs = model.commandLogs.get(model.selectedCommand);
            if ((commandLogs != null) && (commandLogs.selectedStep != null)) {
                sl = commandLogs.stepLogs.get(commandLogs.selectedStep);
            }
        }
        console.setEnabled(sl != null);
        console.setContent(sl == null ? null : String.join("\n", sl.logLines));
        console.setTextAbove(sl == null ? "" : sl.command);
        console.setTextBelow(sl == null ? "" : sl.getSummary());
        console.setTextBelowColor(sl == null || sl.success ? "green" : "red");
    }



}

