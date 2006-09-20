package thinlet;

import java.awt.Cursor;
import java.awt.Rectangle;

import thinlet.api.ModalWidget;
import thinlet.api.Widget;
import thinlet.help.MouseInteraction;
import thinlet.lwjgl.GLColor;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.MouseEvent;

public final class DialogWidget extends PanelWidget implements ModalWidget
{

private boolean modal = false;
private boolean resizable = false;
private boolean closable = false;
private boolean maximizable = false;
private boolean iconifiable = false;

public DialogWidget(ThinletDesktop desktop)
{ super(desktop); }

public boolean isModal()
{ return modal; }

public void setModal(boolean modal)
{ this.modal = modal; }

public boolean isResizable()
{ return resizable; }

public void setResizable(boolean resizable)
{ this.resizable = resizable; }

public boolean isClosable()
{ return closable; }

public void setClosable(boolean closable)
{ this.closable = closable; }

public boolean isMaximizable()
{ return maximizable; }

public void setMaximizable(boolean maximizable)
{ this.maximizable = maximizable; }

public boolean isIconifiable()
{ return iconifiable; }

public void setIconifiable(boolean iconifiable)
{ this.iconifiable = iconifiable; }

@Override
public void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	if (part == "header")
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
			Rectangle bounds = getBounds();
			mouseInteraction.referencex = event.getX() - bounds.x;
			mouseInteraction.referencey = event.getY() - bounds.y;
			desktop.moveToFront(this);
		}
		else if (id == InputEventType.MOUSE_DRAGGED)
		{
			Rectangle bounds = getBounds();
			Rectangle parents = parent().getBounds();
			int mx = Math.max(0, Math.min(event.getX() - mouseInteraction.referencex, parents.width - bounds.width));
			int my = Math.max(0, Math.min(event.getY() - mouseInteraction.referencey, parents.height - bounds.height));
			if ((bounds.x != mx) || (bounds.y != my))
			{ // repaint the union of the previous and next bounds
				repaint(Math.min(bounds.x, mx), Math.min(bounds.y, my),
					bounds.width + Math.abs(mx - bounds.x), bounds.height + Math.abs(my - bounds.y));
				bounds.x = mx; bounds.y = my;
			}
		}
	}
	else if (!processScroll(mouseInteraction, event, part) && (part != null))
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
			mouseInteraction.referencex = event.getX();
			mouseInteraction.referencey = event.getY();
		}
		else if (id == InputEventType.MOUSE_DRAGGED)
		{
			repaint();
			Rectangle bounds = getBounds();
			if ((part == ":nw") || (part == ":n") || (part == ":ne"))
			{
				bounds.y += event.getY() - mouseInteraction.referencey;
				bounds.height -= event.getY() - mouseInteraction.referencey;
			}
			if ((part == ":ne") || (part == ":e") || (part == ":se"))
			{
				bounds.width += event.getX() - mouseInteraction.referencex;
			}
			if ((part == ":sw") || (part == ":s") || (part == ":se"))
			{
				bounds.height += event.getY() - mouseInteraction.referencey;
			}
			if ((part == ":nw") || (part == ":w") || (part == ":sw"))
			{
				bounds.x += event.getX() - mouseInteraction.referencex;
				bounds.width -= event.getX() - mouseInteraction.referencex;
			}
			mouseInteraction.referencex = event.getX();
			mouseInteraction.referencey = event.getY();
			doLayout();
			repaint();
		}
		else if (id == InputEventType.MOUSE_ENTERED)
		{
			desktop.setCursor(Cursor.getPredefinedCursor(
				(part == ":n") ? Cursor.N_RESIZE_CURSOR :
				(part == ":ne") ? Cursor.NE_RESIZE_CURSOR :
				(part == ":e") ? Cursor.E_RESIZE_CURSOR :
				(part == ":se") ? Cursor.SE_RESIZE_CURSOR :
				(part == ":s") ? Cursor.S_RESIZE_CURSOR :
				(part == ":sw") ? Cursor.SW_RESIZE_CURSOR :
				(part == ":w") ? Cursor.W_RESIZE_CURSOR :
					Cursor.NW_RESIZE_CURSOR));
		}
		else if (id == InputEventType.MOUSE_EXITED)
		{
			desktop.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}

@Override
public void findSubComponent(MouseInteraction mouseInteraction, int x, int y)
{
	Rectangle bounds = getBounds();
	int block = desktop.getBlockSize();
//	boolean resizable = isResizable();
	if (resizable && (x < 4))
	{
		mouseInteraction.insidepart = (y < block) ? ":nw" :
			(y >= bounds.height - block) ? ":sw" : ":w";
	}
	else if (resizable && (y < 4))
	{
		mouseInteraction.insidepart = (x < block) ? ":nw" :
			(x >= bounds.width - block) ? ":ne" : ":n";
	}
	else if (resizable && (x >= bounds.width - 4))
	{
		mouseInteraction.insidepart = (y < block) ? ":ne" :
			(y >= bounds.height - block) ? ":se" : ":e";
	}
	else if (resizable && (y >= bounds.height - 4))
	{
		mouseInteraction.insidepart = (x < block) ? ":sw" :
			(x >= bounds.width - block) ? ":se" : ":s";
	}
	else
	{
		int titleheight = getTitleHeight();
		if (y < 4 + titleheight)
			mouseInteraction.insidepart = "header";
	}
	
	if ((mouseInteraction.insidepart == null) && !findScroll(mouseInteraction, x, y))
	{
		Rectangle port = getPort();
		if (port != null)
		{ // content scrolled
			Rectangle view = getView();
			x += view.x - port.x; y += view.y - port.y;
		}
		
		for (Widget comp : this)
		{
			if (comp.findComponent(mouseInteraction, x, y))
				break;
		}
	}
}

/** Paint dialog button. */
private void paintDialogButton(LwjglWidgetRenderer renderer, int x, int y, int width, int height, char type)
{
	renderer.paintBorderAndBackground(this, x, y, width, height, true, true, true, true, 'g');
	renderer.setColor(GLColor.BLACK);
	switch (type)
	{
		case 'c': // closable dialog button
			renderer.drawLine(x + 3, y + 4, x + width - 5, y + height - 4);
			renderer.drawLine(x + 3, y + 3, x + width - 4, y + height - 4);
			renderer.drawLine(x + 4, y + 3, x + width - 4, y + height - 5);
			renderer.drawLine(x + width - 5, y + 3, x + 3, y + height - 5);
			renderer.drawLine(x + width - 4, y + 3, x + 3, y + height - 4);
			renderer.drawLine(x + width - 4, y + 4, x + 4, y + height - 4);
			break;
		case 'm': // maximizable dialog button
			renderer.drawRect(x + 3, y + 3, width - 7, height - 7);
			renderer.drawLine(x + 4, y + 4, x + width - 5, y + 4);
			break;
		case 'i': // iconifiable dialog button
			renderer.fillRect(x + 3, y + height - 5, width - 6, 2);
			break;
		default :
			throw new IllegalArgumentException(""+type);
	}
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	int titleheight = getTitleHeight();
	renderer.paintIconAndText(this, 0, 0, bounds.width, 3 + titleheight,
		true, true, false, true, 1, 2, 1, 2, false, 'g', false);
	int controlx = bounds.width - titleheight - 1;
	if (isClosable())
	{
		paintDialogButton(renderer, controlx, 3, titleheight - 2, titleheight - 2, 'c');
		controlx -= titleheight;
	}
	if (isMaximizable())
	{
		paintDialogButton(renderer, controlx, 3, titleheight - 2, titleheight - 2, 'm');
		controlx -= titleheight;
	}
	if (isIconifiable())
	{
		paintDialogButton(renderer, controlx, 3, titleheight - 2, titleheight - 2, 'i');
	}
	// lower part excluding titlebar
	renderer.paintRect(0, 3 + titleheight, bounds.width, bounds.height - 3 - titleheight,
		renderer.c_border, renderer.c_press, false, true, true, true, true);
	renderer.paintBorderAndBackground(this, // content area
		3, 3 + titleheight, bounds.width - 6, bounds.height - 6 - titleheight,
		true, true, true, true, 'b');
	
	if (getPort() != null)
		paintScroll(renderer, false, enabled);
	else
		paintAll(renderer, enabled);
}

@Override
public void paintScrollableContent(LwjglWidgetRenderer renderer, boolean enabled)
{ paintAll(renderer, enabled); }

}
