package cloud.kpipe.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

public class LeftRightPanel extends HorizontalLayout {

    private static final double LEFT_REL_SIZE = 3;
    private static final double RIGHT_REL_SIZE = 1;

    public LeftRightPanel(VerticalLayout left, VerticalLayout right) {
        setSpacing(false);
        setPadding(false);
        left.setWidth(null); // allow flex to control width
        right.setWidth(null); // allow flex to control width
        setWidthFull(); // ensure layout uses full available width

        // Add components to layout
        add(left, right);

        // Set relative grow factors
        setFlexGrow(LEFT_REL_SIZE, left);
        setFlexGrow(RIGHT_REL_SIZE, right);

        left.getElement().getStyle().set("flex", "0 0 75%");
        right.getElement().getStyle().set("flex", "0 0 25%");
        left.getElement().getStyle().set("min-width", "0");  // allow shrinking
        right.getElement().getStyle().set("min-width", "0"); // allow shrinking

        // Optionally set height or other styling
        setHeightFull();
        left.setHeightFull();
        right.setHeightFull();
    }

}
