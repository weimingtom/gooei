package thinlet.xml;

import thinlet.impl.ButtonWidget;
import thinlet.impl.CheckBoxMenuElement;
import thinlet.impl.CheckBoxWidget;
import thinlet.impl.ComboBoxItem;
import thinlet.impl.ComboBoxWidget;
import thinlet.impl.DialogWidget;
import thinlet.impl.LabelWidget;
import thinlet.impl.ListItem;
import thinlet.impl.ListWidget;
import thinlet.impl.MenuBarWidget;
import thinlet.impl.ActionMenuElement;
import thinlet.impl.SimpleMenuContainer;
import thinlet.impl.PanelWidget;
import thinlet.impl.PopupMenuElement;
import thinlet.impl.ProgressBarWidget;
import thinlet.impl.SeparatorMenuElement;
import thinlet.impl.SliderWidget;
import thinlet.impl.SpacerWidget;
import thinlet.impl.SpinBoxWidget;
import thinlet.impl.SplitPaneWidget;
import thinlet.impl.TabWidget;
import thinlet.impl.TabbedPaneWidget;
import thinlet.impl.TableCell;
import thinlet.impl.TableColumn;
import thinlet.impl.TableHeader;
import thinlet.impl.TableRow;
import thinlet.impl.TableWidget;
import thinlet.impl.TextAreaWidget;
import thinlet.impl.TextFieldWidget;
import thinlet.impl.ThinletDesktop;
import thinlet.impl.TreeNode;
import thinlet.impl.TreeWidget;

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
	if ("popupmenu".equals(classname)) return new PopupMenuElement(desktop);
	
	throw new RuntimeException("INVALID: "+classname);
}

}
