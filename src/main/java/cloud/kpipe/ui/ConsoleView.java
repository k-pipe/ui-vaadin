// ConsoleView.java
package cloud.kpipe.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("console")
@CssImport("./styles/console.css") // see CSS below
public class ConsoleView extends VerticalLayout {

    private final Div consoleContainer;
    private final Pre consolePre;

    private final Div textAbove;
    private final Div textBelow;

    public ConsoleView() {
        setPadding(true);
        setSizeFull();
        setAlignItems(Alignment.STRETCH);

        consoleContainer = new Div();
        consoleContainer.addClassName("console-container");
        consoleContainer.setWidthFull();
        //consoleContainer.setWidth("500px"); // tweak as needed
        //consoleContainer.setHeight("360px"); // tweak as needed
        consoleContainer.getStyle().set("overflow-y", "scroll"); // <- always shows vertical scrollbar
        consoleContainer.getStyle().set("overflow-x", "auto");   // <- horizontal scrollbar if needed
        //        consoleContainer.getStyle().set("overflow", "auto"); // enables scrollbar

        consolePre = new Pre();
        consolePre.addClassName("console-pre");
        consolePre.getStyle().set("margin", "0");
        consolePre.setText(""); // start empty

        consoleContainer.add(consolePre);

        textAbove = new Div();
        textAbove.getStyle().set("color", "purple");
        textAbove.getStyle().set("font-family", "ui-monospace, SFMono-Regular, Menlo, Monaco, \"Roboto Mono\", \"Courier New\", monospace");
        textAbove.getStyle().set("font-size", "10px");

        textBelow = new Div();
        textBelow.setText("Text Below");
        textBelow.getStyle().set("color", "green");

        add(textAbove, consoleContainer, textBelow);
        consoleContainer.setSizeFull();
        setSizeFull();
    }

    /** Append a line and auto-scroll to bottom */
    public void appendLine(String line) {
        String current = consolePre.getText();
        String next = current + line + "\n";
        consolePre.setText(next);

        // scroll the container to bottom (client-side)
        consoleContainer.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    /** Replace whole contents (if needed) */
    public void setContent(String content) {
        consolePre.setText(content);
        consoleContainer.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    public void setTextAbove(String text) {
        textAbove.setText(text);
    }

    public void setTextBelow(String text) {
        textBelow.setText(text);
    }

    public void setTextBelowColor(String color) {
        textBelow.getStyle().set("color", color);
    }
}
