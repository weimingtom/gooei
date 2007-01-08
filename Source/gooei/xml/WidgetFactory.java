package gooei.xml;

import de.ofahrt.gooei.impl.ActionMenuElement;
import de.ofahrt.gooei.impl.ButtonWidget;
import de.ofahrt.gooei.impl.CheckBoxMenuElement;
import de.ofahrt.gooei.impl.CheckBoxWidget;
import de.ofahrt.gooei.impl.ComboBoxItem;
import de.ofahrt.gooei.impl.ComboBoxWidget;
import de.ofahrt.gooei.impl.DialogWidget;
import de.ofahrt.gooei.impl.LabelWidget;
import de.ofahrt.gooei.impl.ListItem;
import de.ofahrt.gooei.impl.ListWidget;
import de.ofahrt.gooei.impl.MenuBarWidget;
import de.ofahrt.gooei.impl.PanelWidget;
import de.ofahrt.gooei.impl.PopupMenuElementImpl;
import de.ofahrt.gooei.impl.ProgressBarWidget;
import de.ofahrt.gooei.impl.SeparatorMenuElement;
import de.ofahrt.gooei.impl.SimpleMenuContainer;
import de.ofahrt.gooei.impl.SliderWidget;
import de.ofahrt.gooei.impl.SpacerWidget;
import de.ofahrt.gooei.impl.SpinBoxWidget;
import de.ofahrt.gooei.impl.SplitPaneWidget;
import de.ofahrt.gooei.impl.TabWidget;
import de.ofahrt.gooei.impl.TabbedPaneWidget;
import de.ofahrt.gooei.impl.TableCell;
import de.ofahrt.gooei.impl.TableColumn;
import de.ofahrt.gooei.impl.TableHeader;
import de.ofahrt.gooei.impl.TableRow;
import de.ofahrt.gooei.impl.TableWidget;
import de.ofahrt.gooei.impl.TextAreaWidget;
import de.ofahrt.gooei.impl.TextFieldWidget;
import de.ofahrt.gooei.impl.ThinletDesktop;
import de.ofahrt.gooei.impl.TreeNode;
import de.ofahrt.gooei.impl.TreeWidget;

public class WidgetFactory
{

public Object createWidget(ThinletDesktop desktop, String classname)
{
	// widgets
	if ("button".equals(classname)) return new ButtonWidget(desktop);
	if ("checkbox".equals(classname)) return new CheckBoxWidget(desktop);
	if ("combobox".equals(classname)) return new ComboBoxWidget(desktop);
	if ("dialog".equals(classname)) return new DialogWidget(desktop);
	if ("label".equals(classname)) return new LabelWidget(desktop);
	if ("list".equals(classname)) return new ListWidget(desktop);
	if ("menubar".equals(classname)) return new MenuBarWidget(desktop);
	if ("panel".equals(classname)) return new PanelWidget(desktop);
	if ("progressbar".equals(classname)) return new ProgressBarWidget(desktop);
	if ("slider".equals(classname)) return new SliderWidget(desktop);
	if ("spacer".equals(classname)) return new SpacerWidget(desktop);
	if ("spinbox".equals(classname)) return new SpinBoxWidget(desktop);
	if ("splitpane".equals(classname)) return new SplitPaneWidget(desktop);
	if ("tab".equals(classname)) return new TabWidget(desktop);
	if ("table".equals(classname)) return new TableWidget(desktop);
	if ("tabbedpane".equals(classname)) return new TabbedPaneWidget(desktop);
	if ("textarea".equals(classname)) return new TextAreaWidget(desktop);
	if ("textfield".equals(classname)) return new TextFieldWidget(desktop);
	if ("tree".equals(classname)) return new TreeWidget(desktop);
	
	// elements: menu items
	if ("separator".equals(classname)) return new SeparatorMenuElement();
	if ("menu".equals(classname)) return new SimpleMenuContainer();
	if ("menuitem".equals(classname)) return new ActionMenuElement();
	if ("checkboxmenuitem".equals(classname)) return new CheckBoxMenuElement();
	
	// elements: other
	if ("header".equals(classname)) return new TableHeader();
	if ("item".equals(classname)) return new ListItem();
	if ("node".equals(classname)) return new TreeNode();
	if ("row".equals(classname)) return new TableRow();
	if ("cell".equals(classname)) return new TableCell();
	if ("choice".equals(classname)) return new ComboBoxItem();
	if ("column".equals(classname)) return new TableColumn();
	if ("popupmenu".equals(classname)) return new PopupMenuElementImpl(desktop);
	
	throw new RuntimeException("INVALID: "+classname);
}

}
