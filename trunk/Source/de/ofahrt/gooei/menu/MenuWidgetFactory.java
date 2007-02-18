package de.ofahrt.gooei.menu;

import gooei.xml.AbstractWidgetFactory;

public class MenuWidgetFactory extends AbstractWidgetFactory
{

public MenuWidgetFactory()
{ init(); }

private void init()
{
	add("panel", Panel.class);
	add("label", Label.class);
	add("stack", Stack.class);
	add("list", List.class);
}

}
