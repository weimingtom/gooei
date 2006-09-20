package thinlet.help;

@Deprecated
public class ThinletDTD
{

private static final Object[] dtd = createDTD();

private static Object[] createDTD()
{
	Integer integer_1 = new Integer(-1);
	Integer integer0 = new Integer(0);
	Integer integer1 = new Integer(1);
	String[] orientation = { "horizontal", "vertical" };
	String[] leftcenterright = { "left", "center", "right" };
	String[] selections = { "single", "interval", "multiple" }; //+none
	return new Object[] {
		// class, superclass, properties
		//  type,     name,   whattodoonchange, default value
		"component", null, new Object[][] {
			{ "string", "name", null, null },
			{ "boolean", "enabled", "paint", Boolean.TRUE },
			{ "boolean", "visible", "parent", Boolean.TRUE },
			{ "string", "tooltip", null, null },
			{ "font", "font", "validate", null },
			{ "color", "foreground", "paint", null },
			{ "color", "background", "paint", null },
			{ "integer", "width", "validate", integer0 },
			{ "integer", "height", "validate", integer0 },
			{ "integer", "colspan", "validate", integer1 },
			{ "integer", "rowspan", "validate", integer1 },
			{ "integer", "weightx", "validate", integer0 },
			{ "integer", "weighty", "validate", integer0 },
			{ "choice", "halign", "validate", new String[] { "fill", "center", "left", "right" } },
			{ "choice", "valign", "validate", new String[] { "fill", "center", "top", "bottom" } },
			// component class String null*
			// parent Object null
			// (bounds) Rectangle 0 0 0 0
			{ "property", "property", null, null },
			{ "method", "init" },           // Ok
			{ "method", "focuslost" },      // Ok
			{ "method", "focusgained" } },  // Ok
		"spacer", "component", null,
		"label", "component", new Object[][] {
			{ "string", "text", "validate", null },
			{ "icon", "icon", "validate", null },
			{ "choice", "alignment", "validate", leftcenterright },
			{ "integer", "mnemonic", "paint", integer_1 },
			{ "component", "for", null, null } }, // Ok
		"button", "label", new Object[][] {
			{ "method", "action" }, // Ok
			{ "choice", "type", "paint", new String[] { "normal", "default", "cancel", "link" } } },
		"checkbox", "label", new Object[][] {
			{ "boolean", "selected", "paint", Boolean.FALSE }, //...group
			{ "string", "group", "paint", null }, //...group
			{ "method", "action" } }, // Ok
		"togglebutton", "checkbox", null,
		"combobox", "textfield", new Object[][] {
			{ "icon", "icon", "validate", null },
			{ "integer", "selected", "layout", integer_1 } },
		"textfield", "component", new Object[][] {
			{ "string", "text", "layout", "" },
			{ "integer", "columns", "validate", integer0 },
			{ "boolean", "editable", "paint", Boolean.TRUE },
			{ "choice", "alignment", "validate", leftcenterright },
			{ "integer", "start", "layout", integer0 },
			{ "integer", "end", "layout", integer0 },
			{ "method", "action" },    // Ok
			{ "method", "insert" },    // Ok
			{ "method", "remove" },    // Ok
			{ "method", "caret" },     // Ok
			{ "method", "perform" } }, // Ok
		"passwordfield", "textfield", null,
		"textarea", "textfield", new Object[][] {
			{ "integer", "rows", "validate", integer0 },
			{ "boolean", "border", "validate", Boolean.TRUE },
			{ "boolean", "wrap", "layout", Boolean.FALSE } },
		"tabbedpane", "component", new Object[][] {
			{ "choice", "placement", "validate",
				new String[] { "top", "left", "bottom", "right", "stacked" } },
			{ "integer", "selected", "paint", integer0 },
			{ "method", "action" } }, // Ok
		"panel", "component", new Object[][] {
			{ "integer", "columns", "validate", integer0 },
			{ "integer", "top", "validate", integer0 },
			{ "integer", "left", "validate", integer0 },
			{ "integer", "bottom", "validate", integer0 },
			{ "integer", "right", "validate", integer0 },
			{ "integer", "gap", "validate", integer0 },
			{ "string", "text", "validate", null },
			{ "icon", "icon", "validate", null },
			{ "boolean", "border", "validate", Boolean.FALSE },
			{ "boolean", "scrollable", "validate", Boolean.FALSE } },
		"dialog", "panel", new Object[][] {
			{ "boolean", "modal", null, Boolean.FALSE },
			{ "boolean", "resizable", null, Boolean.FALSE },
			{ "boolean", "closable", "paint", Boolean.FALSE },
			{ "boolean", "maximizable", "paint", Boolean.FALSE },
			{ "boolean", "iconifiable", "paint", Boolean.FALSE },
			{ "choice", "alignment", "validate", leftcenterright } },
		"desktop", "component", null,
		"spinbox", "textfield", new Object[][] {
			{ "integer", "minimum", null, new Integer(Integer.MIN_VALUE) },
			{ "integer", "maximum", null, new Integer(Integer.MAX_VALUE) },
			{ "integer", "step", null, integer1 },
			{ "integer", "value", null, integer0 } }, // == text? deprecated
		"progressbar", "component", new Object[][] {
			{ "choice", "orientation", "validate", orientation },
			{ "integer", "minimum", "paint", integer0 }, //...checkvalue
			{ "integer", "maximum", "paint", new Integer(100) },
			{ "integer", "value", "paint", integer0 } },
			// change stringpainted
		"slider", "progressbar", new Object[][] {
			{ "integer", "unit", null, new Integer(5) },
			{ "integer", "block", null, new Integer(25) },
			{ "method", "action" } }, // Ok
			// minor/majortickspacing
			// inverted
			// labelincrement labelstart
		"splitpane", "component", new Object[][] {
			{ "choice", "orientation", "validate", orientation },
			{ "integer", "divider", "layout", integer_1 } },
		"list", "component", new Object[][] {
			{ "choice", "selection", "paint", selections },
			{ "method", "action" },   // Ok
			{ "method", "perform" },  // Ok
			{ "boolean", "line", "validate", Boolean.TRUE } },
		"table", "list", new Object[][] {
			/*{ "choice", "selection",
				new String[] { "singlerow", "rowinterval", "multiplerow",
					"cell", "cellinterval",
					"singlecolumn", "columninterval", "multiplecolumn" } }*/ },
		"header", "component", null,
			// reordering allowed
			// autoresize mode: off next (column boundries) subsequents last all columns
			// column row selection
			// selection row column cell
			// editing row/column
		"row", "component", new Object[][] {
			{ "boolean", "selected", null, Boolean.FALSE } },
		"tree", "list", new Object[][] {
			{ "boolean", "angle", null, Boolean.FALSE },
			{ "method", "expand" },     // Ok
			{ "method", "collapse" } }, // Ok
		"menubar", "component", null,
		"popupmenu", "component", new Object[][] {
			{ "method", "menushown" } }, // Ok
		
		// this did not originally inherit component
		"element", "component", new Object[][] {
			{ "string", "name", null, null },
			{ "boolean", "enabled", "paint", Boolean.TRUE },
			{ "string", "text", "parent", null },
			{ "icon", "icon", "parent", null },
			{ "choice", "alignment", "parent", leftcenterright },
			{ "string", "tooltip", null, null },
			{ "font", "font", "validate", null },
			{ "color", "foreground", "paint", null },
			{ "color", "background", "paint", null },
			{ "property", "property", null, null } },
		"choice", "element", null,
		"tab", "element", new Object[][] {
			{ "integer", "mnemonic", "paint", integer_1 } },
		"item", "element", new Object[][] {
			{ "boolean", "selected", null, Boolean.FALSE } },
		"column", "element", new Object[][] {
			{ "integer", "width", null, new Integer(80) },
			{ "choice", "sort", null, new String[] { "none", "ascent", "descent" } } },
		"cell", "element", null,
		"node", "element", new Object[][] {
			{ "boolean", "selected", null, Boolean.FALSE },
			{ "boolean", "expanded", null, Boolean.TRUE } },
		
		"menuelement", "element", null,
		"separator", "menuelement", null,
		"menu", "menuelement", new Object[][] {
			{ "integer", "mnemonic", "paint", integer_1 } },
		"menuitem", "menuelement", new Object[][] {
			{ "keystroke", "accelerator", null, null },
			{ "method", "action" }, // Ok
			{ "integer", "mnemonic", "paint", integer_1 } },
		"checkboxmenuitem", "menuitem", new Object[][] {
			{ "boolean", "selected", "paint", Boolean.FALSE }, //...group
			{ "string", "group", "paint", null } }, //...group
		
		":toplevel", "component", new Object[][] {
			{ "boolean", "modal", null, Boolean.FALSE } },
		":popup", ":toplevel", null,
		":combolist", ":toplevel", null,
	};
}

}
