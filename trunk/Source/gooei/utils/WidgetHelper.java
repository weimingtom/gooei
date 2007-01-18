package gooei.utils;

import gooei.ContainerWidget;
import gooei.Widget;

public class WidgetHelper
{

public static Widget findWidget(Widget current, String name)
{
	if (name.equals(current.getName())) return current;
	Widget found;
	
	// otherwise search in its subcomponents
	if (current instanceof ContainerWidget<?>)
	{
		for (Widget comp : (ContainerWidget<?>) current)
		{
			found = findWidget(comp, name);
			if (found != null)
				return found;
		}
	}
	
	// search in table header
/*	if (this instanceof TableWidget)
	{
		TableHeader header = ((TableWidget) this).getHeaderWidget();
		if ((header != null) && ((found = header.findWidget(fname)) != null)) return found;
	}*/
	
	// search in component's popupmenu
//	PopupMenuWidget popupmenu = getPopupMenuWidget();
//	if ((popupmenu != null) && ((found = popupmenu.findWidget(fname)) != null)) return found;
	return null;
}

}
