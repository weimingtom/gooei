package de.ofahrt.gooei.impl;

import gooei.*;
import gooei.input.InputEventType;
import gooei.input.MouseEvent;

import java.awt.Rectangle;

/**
 * Abstract parent of {@link MenuBarWidget} and {@link PopupWidgetImpl}, that
 * contains common base code.
 */
public abstract class MenuContainerWidget extends AbstractWidget
{

private PopupWidgetImpl popupWidget;
private MenuElement selectedWidget;

public MenuContainerWidget(ThinletDesktop desktop)
{ super(desktop); }

public PopupWidgetImpl getPopupWidget()
{ return popupWidget; }

public void setPopupWidget(PopupWidgetImpl popupWidget)
{ this.popupWidget = popupWidget; }

public MenuElement getSelectedWidget()
{ return selectedWidget; }

public void setSelectedWidget(MenuElement selectedWidget)
{ this.selectedWidget = selectedWidget; }

public PopupWidgetImpl popupMenu()
{
	PopupWidgetImpl popup = getPopupWidget();
	MenuElement selected = getSelectedWidget();
	if (popup != null)
	{
		if (popup.getMenuWidget() == selected) return null; // is already open
		popup.setSelectedWidget(null);
		popup.repaint();
		desktop.removeChild(popup);
		setPopupWidget(null);
		desktop.checkLocation(popup);
		popup.popupMenu(); // remove recursively
	}
	
	// pop up the selected menu only 
	if (!(selected instanceof MenuContainerElement))
		return null;
	
	// calculates the bounds of the previous menubar/:popup relative to the root desktop
	int menux = 0, menuy = 0, menuwidth = 0, menuheight = 0;
	for (Widget comp = this; comp != null; comp = comp.parent())
	{
		Rectangle r = comp.getBounds();
		menux += r.x;
		menuy += r.y;
		if (comp instanceof ScrollableWidget)
		{
			Rectangle view = ((ScrollableWidget) comp).getView();
			if (view != null)
			{
				menux -= view.x;
				menuy -= view.y;
				Rectangle port = ((ScrollableWidget) comp).getPort();
				menux += port.x;
				menuy+= port.y;
			}
		}
		if (comp == this)
		{
			menuwidth = r.width;
			menuheight = r.height;
		}
	}
	
	// create the popup, popup.menu -> menu,
	// menubar|popup.popup -> popup
	boolean menubar = this instanceof MenuBarWidget;
	popup = new PopupWidgetImpl(desktop, (MenuContainerElement<?>) selected);
	setPopupWidget(popup);
	desktop.insertItem(popup, 0);
	if (menubar) desktop.setPopupOwner((PopupOwner) this);
	
	Rectangle menubounds = selected.getBounds();
	popup.popup(
		menubar ? 'D' : 'R',
		menubar ? (menux + menubounds.x) : menux, menuy + menubounds.y,
		menubar ? menubounds.width : menuwidth,
		menubar ? menuheight : menubounds.height, menubar ? 1 : 3);
	return popup;
}

protected void repaintComponent(MenuElement part)
{
	Rectangle b = getBounds();
	Rectangle r = part.getBounds();
	repaint(b.x + r.x, b.y + r.y, b.width, r.height);
}

/*@Override
public boolean handleKeyPress(KeyboardEvent event)
{ return false; }*/

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if (((id == InputEventType.MOUSE_ENTERED) || (id == InputEventType.DRAG_ENTERED)) &&
			(part != null) && ((MenuElement) part).isEnabled())
	{
		setSelectedWidget((MenuElement) part);
		popupMenu();
		repaintComponent((MenuElement) part);
	}
	else if ((id == InputEventType.MOUSE_RELEASED) &&
			((part != null)/* || ((mouseInteraction.insidepart != null) && (this instanceof PopupMenuWidget))*/))
	{
		if ((mouseInteraction.insidepart == null) || !(mouseInteraction.insidepart instanceof MenuContainerElement))
		{
			if ((mouseInteraction.insidepart != null) && ((Element) mouseInteraction.insidepart).isEnabled())
			{
				if (mouseInteraction.insidepart instanceof CheckBoxMenuElement)
					((CheckBoxMenuElement) mouseInteraction.insidepart).changeCheck();
				else
					((ActionMenuElement) mouseInteraction.insidepart).invokeAction();
			}
			desktop.closeup();
		}
	}
	else if (((id == InputEventType.MOUSE_EXITED) || (id == InputEventType.DRAG_EXITED)) &&
			(part != null) && ((Element) part).isEnabled())
	{
		if (!(mouseInteraction.insidepart instanceof MenuContainerElement))
			setSelectedWidget(null);
		repaintComponent((MenuElement) part);
	}
}

}
