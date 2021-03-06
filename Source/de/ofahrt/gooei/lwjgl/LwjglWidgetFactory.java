package de.ofahrt.gooei.lwjgl;

import gooei.xml.AbstractWidgetFactory;
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
import de.ofahrt.gooei.impl.TreeNode;
import de.ofahrt.gooei.impl.TreeWidget;

public class LwjglWidgetFactory extends AbstractWidgetFactory
{

public LwjglWidgetFactory()
{ init(); }

private void init()
{
	// Widgets
	add("button", ButtonWidget.class);
	add("checkbox", CheckBoxWidget.class);
	add("combobox", ComboBoxWidget.class);
	add("dialog", DialogWidget.class);
	add("label", LabelWidget.class);
	
	add("list", ListWidget.class);
	add("menubar", MenuBarWidget.class);
	add("panel", PanelWidget.class);
	add("progressbar", ProgressBarWidget.class);
	add("slider", SliderWidget.class);
	
	add("spacer", SpacerWidget.class);
	add("spinbox", SpinBoxWidget.class);
	add("splitpane", SplitPaneWidget.class);
	add("tab", TabWidget.class);
	add("table", TableWidget.class);
	
	add("tabbedpane", TabbedPaneWidget.class);
	add("textarea", TextAreaWidget.class);
	add("textfield", TextFieldWidget.class);
	add("tree", TreeWidget.class);
	
	
	// Menu Elements
	add("separator", SeparatorMenuElement.class);
	add("menu", SimpleMenuContainer.class);
	add("menuitem", ActionMenuElement.class);
	add("checkboxmenuitem", CheckBoxMenuElement.class);
	
	// Other Elements
	add("header", TableHeader.class);
	add("item", ListItem.class);
	add("node", TreeNode.class);
	add("row", TableRow.class);
	add("cell", TableCell.class);
	add("choice", ComboBoxItem.class);
	add("column", TableColumn.class);
	add("popupmenu", PopupMenuElementImpl.class);
}
}
