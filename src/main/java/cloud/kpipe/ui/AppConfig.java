package cloud.kpipe.ui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;

/* needed so changes can be pushed from backend to frontend  */
@Push  // enable push globally
@Theme("starter-theme")
public class AppConfig implements AppShellConfigurator {
}
