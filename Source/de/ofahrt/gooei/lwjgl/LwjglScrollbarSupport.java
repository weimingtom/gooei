package de.ofahrt.gooei.lwjgl;

import gooei.Desktop;
import gooei.Element;
import gooei.MouseInteraction;
import gooei.Renderer;
import gooei.ScrollableWidget;
import gooei.input.InputEventType;
import gooei.input.MouseEvent;
import gooei.utils.ScrollbarSupport;
import gooei.utils.TimerEventType;

import java.awt.Rectangle;

public final class LwjglScrollbarSupport implements ScrollbarSupport
{

private final Desktop desktop;
private final ScrollableWidget widget;

public LwjglScrollbarSupport(Desktop desktop, ScrollableWidget widget)
{
	this.desktop = desktop;
	this.widget = widget;
}

public void scrollToVisible(int x, int y, int w, int h)
{
	Rectangle v = widget.getView();
	Rectangle p = widget.getPort();
	int vx = Math.max(x + w - p.width, Math.min(v.x, x));
	int vy = Math.max(y + h - p.height, Math.min(v.y, y));
	if ((v.x != vx) || (v.y != vy))
	{
		desktop.repaint(widget);
		v.x = vx;
		v.y = vy;
	}
}

private void findHorizontal(MouseInteraction mouseInteraction, Rectangle horizontal, int x, int y)
{
//	int p, int size, int portsize, int viewp, int viewsize
	int p = x-horizontal.x;
	int size = horizontal.width;
	int portsize = widget.getPort().width;
	int viewsize = widget.getView().width;
	int viewp = widget.getView().x;
	int block = horizontal.height;
	if (p < block)
		mouseInteraction.insidepart = "left";
	else if (p > size - block)
		mouseInteraction.insidepart = "right";
	else
	{
		int track = size - 2 * block;
		if (track < 10)
		{
			mouseInteraction.insidepart = "corner";
			return;
		} // too small
		int knob = Math.max(track * portsize / viewsize, 10);
		int decrease = viewsize == portsize ? 0 : viewp * (track - knob) / (viewsize - portsize);
		if (p < block + decrease) mouseInteraction.insidepart = "lefttrack";
		else if (p < block + decrease + knob) mouseInteraction.insidepart = "hknob";
		else mouseInteraction.insidepart = "righttrack";
	}
}

private void findVertical(MouseInteraction mouseInteraction, Rectangle vertical, int x, int y)
{
	int p = y-vertical.y;
	int size = vertical.height;
	int portsize = widget.getPort().height;
	int viewsize = widget.getView().height;
	int viewp = widget.getView().y;
	int block = vertical.width;
	if (p < block)
		mouseInteraction.insidepart = "up";
	else if (p > size - block)
		mouseInteraction.insidepart = "down";
	else
	{
		int track = size - 2 * block;
		if (track < 10)
		{
			mouseInteraction.insidepart = "corner";
			return;
		} // too small
		int knob = Math.max(track * portsize / viewsize, 10);
		int decrease = viewsize == portsize ? 0 : viewp * (track - knob) / (viewsize - portsize);
		if (p < block + decrease) mouseInteraction.insidepart = "uptrack";
		else if (p < block + decrease + knob) mouseInteraction.insidepart = "vknob";
		else mouseInteraction.insidepart = "downtrack";
	}
}

public final boolean findScroll(MouseInteraction mouseInteraction, int x, int y)
{
	Rectangle port = widget.getPort();
	if ((port == null) || port.contains(x, y)) return false;
	Rectangle horizontal = widget.getHorizontal();
	Rectangle vertical = widget.getVertical();
	if ((horizontal != null) && horizontal.contains(x, y))
		findHorizontal(mouseInteraction, horizontal, x, y);
	else if ((vertical != null) && vertical.contains(x, y))
		findVertical(mouseInteraction, vertical, x, y);
	else
		mouseInteraction.insidepart = "corner";
	return true;
}

public final void repaintScrollablePart(Element part)
{
	// repaint the whole line of its subcomponent
	Rectangle b = widget.getBounds();
	Rectangle p = widget.getPort();
	Rectangle v = widget.getView();
	Rectangle r = part.getBounds();
	if ((r.y + r.height >= v.y) && (r.y <= v.y + p.height))
	{
		desktop.repaint(widget, new Rectangle(b.x+p.x, b.y+p.y - v.y + r.y, p.width, r.height));
	}
}

private final void repaintScrollablePart(String part)
{
	desktop.repaint(widget);
/*	if (!(this instanceof ScrollableWidget)) throw new UnsupportedOperationException();
	if ("left".equals(part) || "right".equals(part))
	{ // horizontal scrollbar button
		widget.repaint(horizontal);
	}
	else if ("up".equals(part) || "down".equals(part))
	{ // vertical scrollbar button
		widget.repaint(vertical);
	}
	else if ("text".equals(part) || "horizontal".equals(part) || "vertical".equals(part))
		desktop.repaint(widget);
	else
		throw new UnsupportedOperationException();*/
}

public final boolean handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event)
{
	InputEventType id = event.getType();
	int x = event.getX(), y = event.getY();
	int block = desktop.getBlockSize();
	if (("up".equals(part)) || ("down".equals(part)) ||
			("left".equals(part)) || ("right".equals(part)))
	{
		if ((id == InputEventType.MOUSE_ENTERED) ||
				(id == InputEventType.MOUSE_EXITED) ||
				(id == InputEventType.MOUSE_PRESSED) ||
				(id == InputEventType.MOUSE_RELEASED))
		{
			if (id == InputEventType.MOUSE_PRESSED)
			{
				if (widget.handleScrollEvent(part))
				{
					desktop.setTimer(TimerEventType.SCROLL, 300L);
					return true;
				}
			}
			else
			{
				if (id == InputEventType.MOUSE_RELEASED)
					desktop.setTimer(null, 0L);
				repaintScrollablePart((String) part);
			}
		}
	}
	else if (("uptrack".equals(part)) || ("downtrack".equals(part)) ||
			("lefttrack".equals(part)) || ("righttrack".equals(part)))
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
			if (widget.handleScrollEvent(part))
			{
				desktop.setTimer(TimerEventType.SCROLL, 300L);
			}
		}
		else if (id == InputEventType.MOUSE_RELEASED)
		{
			desktop.setTimer(null, 0L);
		}
	}
	else if (("vknob".equals(part)) || ("hknob".equals(part)))
	{
		Rectangle port = widget.getPort();
		Rectangle view = widget.getView();
		if (id == InputEventType.MOUSE_PRESSED)
		{
//			Rectangle port = getPort();
//			Rectangle view = getView();
			if (part == "hknob")
				mouseInteraction.referencex = x - view.x * (port.width - 2 * block) / view.width;
			else
				mouseInteraction.referencey = y - view.y * (port.height - 2 * block) / view.height;
		}
		else if (id == InputEventType.MOUSE_DRAGGED)
		{
//			Rectangle port = getPort();
//			Rectangle view = getView();
			if (part == "hknob")
			{
				int viewx = (x - mouseInteraction.referencex) * view.width / (port.width - 2 * block);
				viewx = Math.max(0, Math.min(viewx, view.width - port.width));
				if (view.x != viewx)
				{
					view.x = viewx;
					repaintScrollablePart("horizontal");
				}
			}
			else
			{ // (part == "vknob")
				int viewy = (y - mouseInteraction.referencey) * view.height / (port.height - 2 * block);
				viewy = Math.max(0, Math.min(viewy, view.height - port.height));
				if (view.y != viewy)
				{
					view.y = viewy;
					repaintScrollablePart("vertical");
				}
			}
		}
	}
	else if (part == "corner")
	{
		part = "corner"; // compiler bug
	}
	else
	{
		if (id == InputEventType.MOUSE_PRESSED)
		{
			Rectangle port = widget.getPort();
			if (port != null)
				mouseInteraction.setReference(widget, port.x, port.y);
		}
		return false;
	}
	return true;
}

public boolean handleScrollEvent(Object part)
{
	Rectangle view = widget.getView();
	Rectangle port = widget.getPort();
	int dx = 0;
	int dy = 0;
	if (part == "left") dx = -10;
	else if (part == "lefttrack") dx = -port.width;
	else if (part == "right") dx = 10;
	else if (part == "righttrack") dx = port.width;
	else if (part == "up") dy = -10;
	else if (part == "uptrack") dy = -port.height;
	else if (part == "down") dy = 10;
	else if (part == "downtrack") dy = port.height;
	if (dx != 0)
	{
		dx = (dx < 0) ? Math.max(-view.x, dx) :
			Math.min(dx, view.width - port.width - view.x);
	}
	else if (dy != 0)
	{
		dy = (dy < 0) ? Math.max(-view.y, dy) :
			Math.min(dy, view.height - port.height - view.y);
	}
	else
		return false;
	if ((dx == 0) && (dy == 0)) return false;
	view.x += dx;
	view.y += dy;
	repaintScrollablePart((dx != 0) ? "horizontal" : "vertical");
	return (((part == "left") || (part == "lefttrack")) && (view.x > 0)) ||
		(((part == "right") || (part == "righttrack")) &&
			(view.x < view.width - port.width)) ||
		(((part == "up") || (part == "uptrack")) && (view.y > 0)) ||
		(((part == "down") || (part == "downtrack")) &&
			(view.y < view.height - port.height));
}

private void paintHorizontal(LwjglRenderer renderer, Rectangle horizontal, 
		boolean pressed, boolean inside, boolean isenabled)
{
	Rectangle port = widget.getPort();
	Rectangle view = widget.getView();
	
	int x = horizontal.x;
	int y = horizontal.y;
	int w = horizontal.width;
	int h = horizontal.height;
	int block = h;
	renderer.paintArrow(x, y, block, h,
		'W', isenabled, inside, pressed, "left", true, true, true, false, true);
	renderer.paintArrow(x + w - block, y, block, h,
		'E', isenabled, inside, pressed, "right", true, false, true, true, true);
	
	int track = w - (2 * block);
	if (track < 10)
	{
		renderer.paintRect(x + block, y, track, h,
			isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, true, true, true, true);
	}
	else
	{
		int knob = Math.max(track * port.width / view.width, 10);
		int decrease = view.width == port.width ? 0 : view.x * (track - knob) / (view.width - port.width);
		renderer.paintRect(x + block, y, decrease, h,
			isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, false, true, true, false, true);
		renderer.paintRect(x + block + decrease, y, knob, h,
			isenabled ? renderer.c_border : renderer.c_disable, isenabled ? renderer.c_ctrl : renderer.c_bg, true, true, true, true, true);
		int n = Math.min(5, (knob - 4) / 3);
		renderer.setColor(isenabled ? renderer.c_border : renderer.c_disable);
		int cx = (x + block + decrease) + (knob + 2 - n * 3) / 2;
		for (int i = 0; i < n; i++ )
		{
			renderer.drawLine(cx + i * 3, y + 3, cx + i * 3, y + h - 5);
		}
		int increase = track - decrease - knob;
		renderer.paintRect(x + block + decrease + knob, y, increase, h,
			isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, false, false, true, true, true);
	}
}

private void paintVertical(LwjglRenderer renderer, Rectangle vertical,
		boolean pressed, boolean inside, boolean isenabled)
{
	Rectangle port = widget.getPort();
	Rectangle view = widget.getView();
	
	int x = vertical.x;
	int y = vertical.y;
	int w = vertical.width;
	int h = vertical.height;
	int block = w;
	renderer.paintArrow(x, y, w, block,
		'N', isenabled, inside, pressed, "up", true, true, false, true, false);
	renderer.paintArrow(x, y + h - block, w, block,
		'S', isenabled, inside, pressed, "down", false, true, true, true, false);
	
	int track = h - (2 * block);
	if (track < 10)
	{
		renderer.paintRect(x, y + block, w, track,
			isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, true, true, true, false);
	}
	else
	{
		int knob = Math.max(track * port.height / view.height, 10);
		int decrease = view.height == port.height ? 0 : view.y * (track - knob) / (view.height - port.height);
		renderer.paintRect(x, y + block, w, decrease,
			isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, true, false, false, true, false);
		renderer.paintRect(x, y + block + decrease, w, knob,
			isenabled ? renderer.c_border : renderer.c_disable, isenabled ? renderer.c_ctrl : renderer.c_bg, true, true, true, true, false);
		int n = Math.min(5, (knob - 4) / 3);
		renderer.setColor(isenabled ? renderer.c_border : renderer.c_disable);
		int cy = (y + block + decrease) + (knob + 2 - n * 3) / 2;
		for (int i = 0; i < n; i++ )
		{
			renderer.drawLine(x + 3, cy + i * 3, x + w - 5, cy + i * 3);
		}
		int increase = track - decrease - knob;
		renderer.paintRect(x, y + block + decrease + knob, w, increase,
			isenabled ? renderer.c_border : renderer.c_disable, renderer.c_bg, false, false, true, true, false);
	}
}

public final void paintScrollbars(Renderer r, boolean pressed, boolean inside, boolean isenabled)
{
	LwjglRenderer renderer = (LwjglRenderer) r;
	// draw scrollbars
	Rectangle horizontal = widget.getHorizontal();
	Rectangle vertical = widget.getVertical();
	if (horizontal != null)
		paintHorizontal(renderer, horizontal, pressed, inside, isenabled);
	if (vertical != null)
		paintVertical(renderer, vertical, pressed, inside, isenabled);
}

}
