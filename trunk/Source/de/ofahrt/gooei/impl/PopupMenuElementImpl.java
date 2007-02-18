package de.ofahrt.gooei.impl;

import gooei.*;
import gooei.input.KeyboardEvent;
import gooei.input.Keys;
import gooei.utils.MethodInvoker;

import java.awt.Dimension;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class PopupMenuElementImpl extends AbstractContainerElement<MenuElement> implements PopupOwner, PopupMenuElement
{

private final Desktop desktop;

private PopupWidgetImpl popupWidget;
private MethodInvoker menushownMethod;

public PopupMenuElementImpl(Desktop desktop)
{ this.desktop = desktop; }

public PopupWidgetImpl getPopupWidget()
{ return popupWidget; }

public void setPopupWidget(PopupWidgetImpl popupWidget)
{ this.popupWidget = popupWidget; }

public void setMenuShown(MethodInvoker method)
{ menushownMethod = method; }

protected boolean invokeIt(MethodInvoker method, Object part)
{
	if (method != null)
	{
		method.invoke(part);
		return true;
	}
	return false;
}

public boolean invokeMenuShown()
{ return invokeIt(menushownMethod, null); }


public int getMnemonic()
{ return -1; }

public boolean checkMnemonic(Keys keycode, int modifiers)
{ return false; }


@Override
public boolean acceptChild(Element node)
{ return node instanceof MenuElement; }

@Override
public void remove()
{
	PopupMenuWidget parent = (PopupMenuWidget) parent();
	parent.setPopupMenu(null);
}

public void closePopup()
{
	PopupWidgetImpl popup = getPopupWidget();
	if (popup != null)
	{
		MenuElement selected = popup.getSelectedWidget(); // selected menu of the component
		if (popup.getMenuWidget() == selected) return; // but the currect one
		popup.setSelectedWidget(null);
		popup.repaint();
		desktop.removeChild(popup);
		setPopupWidget(null);
		desktop.checkLocation();
		popup.popupMenu(); // remove recursively
	}
}

public PopupWidgetImpl popupPopup(int x, int y)
{
	// :popup.menu -> popupmenu, popupmenu.:popup -> :popup
	PopupWidgetImpl popup = new PopupWidgetImpl(desktop, this);
	setPopupWidget(popup);
	desktop.setPopup(popup, this);
	
	popup.popup('D', x, y, 0, 0, 0);
	
	// invoke menushown listener
	invokeMenuShown(); // TODO before
	return popup;
}

public Dimension getSize(Desktop unused_dktp, int dx, int dy)
{ throw new UnsupportedOperationException(); }

public void paint(LwjglRenderer renderer, boolean armed)
{ throw new UnsupportedOperationException(); }

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	
	// find the last open popup and the previous one
	PopupWidgetImpl previous = null, last = null;
	for (PopupWidgetImpl i = getPopupWidget(); i != null; i = i.getPopupWidget())
	{
		previous = last;
		last = i;
	}
	
	//selected is the current item of the last, or the previous :popup, or null
	MenuElement selected = last.getSelectedWidget();
	PopupWidgetImpl hotpopup = ((selected != null) || (previous == null)) ? last : previous;
	if ((selected == null) && (previous != null))
		selected = previous.getSelectedWidget();
	
	if ((keycode == Keys.UP) || (keycode == Keys.DOWN))
	{
		MenuElement next = hotpopup.getMenu(selected, keycode == Keys.DOWN);
		if (next != null)
		{
			hotpopup.setSelectedWidget(null);
			hotpopup.popupMenu();
			hotpopup.setSelectedWidget(next);
			hotpopup.repaint();
		}
	}
	else if (keycode == Keys.LEFT)
	{
		if (previous != null)
		{ // close the last :popup
			selected = previous.getSelectedWidget();
			previous.setSelectedWidget(null);
			previous.popupMenu();
			previous.setSelectedWidget(selected);
			previous.repaint(); // , selected
		}
	}
	else if (keycode == Keys.RIGHT)
	{
		if ((previous != null) && (selected == null))
		{ // ?
			last.setSelectedWidget(last.getMenuWidget().getChild(0));
			last.repaint(); // , selected
		}
		else if ((selected != null) && (selected instanceof MenuContainerElement))
		{ // expand menu
			PopupWidgetImpl popup = last.popupMenu();
			popup.setSelectedWidget(popup.getMenu(null, true));
		}
	}
	else if ((keycode == Keys.RETURN) ||
			(keycode == Keys.SPACE) || (keycode == Keys.ESCAPE))
	{
		if ((keycode != Keys.ESCAPE) && (selected != null) && selected.isEnabled())
		{
			if (selected instanceof CheckBoxMenuElement)
				((CheckBoxMenuElement) selected).changeCheck();
			else
				((ActionMenuElement) selected).invokeAction();
		}
		desktop.closePopup();
	}
	else
		return false;
	return true;
}

}
