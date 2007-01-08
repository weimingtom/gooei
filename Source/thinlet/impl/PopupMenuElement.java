package thinlet.impl;

import java.awt.Dimension;

import thinlet.Element;
import thinlet.MenuContainerElement;
import thinlet.MenuElement;
import thinlet.PopupOwner;
import thinlet.Widget;
import thinlet.help.MethodInvoker;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.Keys;

public final class PopupMenuElement extends AbstractContainerElement<MenuElement> implements PopupOwner, MenuContainerElement<MenuElement>
{

private final ThinletDesktop desktop;

private PopupWidget popupWidget;
private MethodInvoker menushownMethod;

public PopupMenuElement(ThinletDesktop desktop)
{ this.desktop = desktop; }

public PopupWidget getPopupWidget()
{ return popupWidget; }

public void setPopupWidget(PopupWidget popupWidget)
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
	Widget parent = (Widget) parent();
	parent.setPopupMenuWidget(null);
}

public void closePopup()
{
	PopupWidget popup = getPopupWidget();
	MenuElement selected = popup.getSelectedWidget(); // selected menu of the component
	if (popup != null)
	{
		if (popup.getMenuWidget() == selected) return; // but the currect one
		popup.setSelectedWidget(null);
		popup.repaint();
		desktop.removeChild(popup);
		setPopupWidget(null);
		desktop.checkLocation(popup);
		popup.popupMenu(); // remove recursively
	}
}

public PopupWidget popupPopup(int x, int y)
{
	// :popup.menu -> popupmenu, popupmenu.:popup -> :popup
	PopupWidget popup = new PopupWidget(desktop, this);
	setPopupWidget(popup);
	desktop.insertItem(popup, 0);
	desktop.setPopupOwner(this);
	
	popup.popup('D', x, y, 0, 0, 0);
	
	// invoke menushown listener
	invokeMenuShown(); // TODO before
	return popup;
}

public Dimension getSize(ThinletDesktop unused_dktp, int dx, int dy)
{ throw new UnsupportedOperationException(); }

public void paint(LwjglWidgetRenderer renderer, boolean armed)
{ throw new UnsupportedOperationException(); }

public boolean handleKeyPress(KeyboardEvent event)
{
	Keys keycode = event.getKeyCode();
	
	// find the last open popup and the previous one
	PopupWidget previous = null, last = null;
	for (PopupWidget i = getPopupWidget(); i != null; i = i.getPopupWidget())
	{
		previous = last;
		last = i;
	}
	
	//selected is the current item of the last, or the previous :popup, or null
	MenuElement selected = last.getSelectedWidget();
	PopupWidget hotpopup = ((selected != null) || (previous == null)) ? last : previous;
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
			PopupWidget popup = last.popupMenu();
			popup.setSelectedWidget(popup.getMenu(null, true));
		}
	}
	else if ((keycode == Keys.RETURN) ||
			(keycode == Keys.SPACE) || (keycode == Keys.ESCAPE))
	{
		if ((keycode != Keys.ESCAPE) && selected.isEnabled())
		{
			if ((selected != null) && (selected instanceof CheckBoxMenuElement))
				((CheckBoxMenuElement) selected).changeCheck();
			else
				((ActionMenuElement) selected).invokeAction();
		}
		desktop.closeup();
	}
	else
		return false;
	return true;
}

}
