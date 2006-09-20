/* Thinlet GUI toolkit - www.thinlet.com
 * Copyright (C) 2002-2005 Robert Bajzat (rbajzat@freemail.hu)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */
package thinlet.awt;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

import thinlet.UIState;
import thinlet.WidgetContainer;
import thinlet.WidgetEnvironment;
import thinlet.help.*;

final class AWTWidgetEnvironment extends Container implements Runnable, WidgetEnvironment
{

private static final long serialVersionUID = 1L;

private transient Thread timer;
private transient TimerEventType timerType;
private transient long watch;

private final WidgetContainer container;
private final UIState state;
private final AWTWidgetRenderer renderer;

public AWTWidgetEnvironment(WidgetContainer container)
{
	this.container = container;
	this.state = container.getState();
	this.renderer = new AWTWidgetRenderer(this, container);
	
	// disable global focus-manager for this component in 1.4
	setFocusTraversalKeysEnabled(false);
	
	// set listeners flags
	enableEvents(AWTEvent.COMPONENT_EVENT_MASK |
		AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
		AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | 
		AWTEvent.MOUSE_WHEEL_EVENT_MASK);
}

public WidgetContainer getContainer()
{ return container; }

public void showContainer()
{
	new AWTFrameLauncher("", this, 320, 320);
}

@Override
public void setFont(Font font)
{
	renderer.setFont(new AWTFont(font));
	super.setFont(font);
	container.getDesktop().validate();
}

public TLFont getDefaultFont()
{ return renderer.getFont(); }

public TLFontMetrics getFontMetrics(TLFont font)
{ return new AWTFontMetrics(getFontMetrics(((AWTFont) font).getFont())); }

public TLFont createFont(String name, int style, int size)
{ return new AWTFont(new Font(name, style, size)); }

public TLColor createColor(int color)
{ return new AWTColor(color); }

public TLColor createColor(int red, int green, int blue)
{ return new AWTColor(red/255.0f, green/255.0f, blue/255.0f); }

public TLColor createColor(int red, int green, int blue, int alpha)
{ return new AWTColor(red/255.0f, green/255.0f, blue/255.0f, alpha/255.0f); }

public int getBlockSize()
{ return renderer.getBlockSize(); }

public Clipboard getSystemClipboard()
{ return getToolkit().getSystemClipboard(); }

@Override
public Dimension getPreferredSize()
{ return container.getDesktop().getPreferredSize(); }

@Override
public void update(Graphics g)
{ paint(g); }

@Override
public void paint(Graphics g)
{
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	renderer.paint(g);
}

/**
 * This component can be traversed using Tab or Shift-Tab keyboard focus traversal,
 * although 1.4 replaced this method by <i>isFocusable</i>,
 * so 1.4 compilers write deprecation warning
 *
 * @return true as focus-transverable component, overwrites the default false value
 */
@Override
public boolean isFocusTraversable()
{ return true; }

// translates awt events into internal events
@Override
protected void processEvent(AWTEvent e)
{
	// evm (touchscreen) events: entered/moved/pressed -> dragged -> dragged/released/exited
	int id = e.getID();
	if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_MOVED) ||
			(id == MouseEvent.MOUSE_EXITED) || (id == MouseEvent.MOUSE_PRESSED) ||
			(id == MouseEvent.MOUSE_DRAGGED) || (id == MouseEvent.MOUSE_RELEASED))
	{
		MouseEvent me = (MouseEvent) e;
		int x = me.getX();
		int y = me.getY();
		boolean popuptrigger = (id == MouseEvent.MOUSE_PRESSED) && me.isMetaDown(); // isPopupTrigger is platform dependent
		state.onMouse(id, x, y, me.getClickCount(), me.getModifiers(), popuptrigger);
	}
	else if (id == MouseEvent.MOUSE_WHEEL)
		state.onScroll(((MouseWheelEvent) e).getWheelRotation());
	else if ((id == KeyEvent.KEY_PRESSED) || (id == KeyEvent.KEY_TYPED))
	{
		KeyEvent ke = (KeyEvent) e;
		int keychar = ke.getKeyChar();
		int keycode = ke.getKeyCode();
		if (state.onKey(id, keychar, keycode, ke.getModifiers(), ke.isActionKey()))
			ke.consume();
	}
	else if (id == FocusEvent.FOCUS_LOST)
		state.onFocusLost();
	else if (id == FocusEvent.FOCUS_GAINED)
		state.onFocus();
	else if ((id == ComponentEvent.COMPONENT_RESIZED) ||
			(id == ComponentEvent.COMPONENT_SHOWN))
		state.onResize();
}

public Icon getIcon(String path)
{
	if ((path == null) || (path.length() == 0))
		return null;
	
	Image image = null;
	try
	{
		URL url = getClass().getResource(path);
		if (url != null)
		{ // contributed by Stefan Matthias Aust
			image = Toolkit.getDefaultToolkit().getImage(url);
		}
	}
	catch (Throwable e)
	{/*IGNORED EXCEPTION*/}
	
	if (image == null)
	{
		try
		{
			InputStream is = getClass().getResourceAsStream(path);
			//InputStream is = ClassLoader.getSystemResourceAsStream(path);
			if (is != null)
			{
				byte[] data = new byte[is.available()];
				is.read(data, 0, data.length);
				image = getToolkit().createImage(data);
				is.close();
			}
			else
			{ // contributed by Wolf Paulus
				image = Toolkit.getDefaultToolkit().getImage(new URL(path));
			}
		}
		catch (Throwable e)
		{/*IGNORED EXCEPTION*/}
	}
	
	if (image != null)
	{
		MediaTracker mediatracker = new MediaTracker(this);
		mediatracker.addImage(image, 1);
		try
		{
			mediatracker.waitForID(1, 5000);
		}
		catch (InterruptedException ie)
		{/*IGNORED EXCEPTION*/}
	}
	return image == null ? null : new AWTIcon((BufferedImage) image);
}

/**
 * A second thread is used to repeat value change events for scrollbar or spinbox
 * during the mouse is pressed, or to pop up tooltip
 */
public synchronized void run()
{
	while (timer == Thread.currentThread())
	{
		try
		{
			if (watch == 0)
			{
				wait(0);
			}
			else
			{
				long current = System.currentTimeMillis();
				if (watch > current)
				{
					wait(watch - current);
				}
				else
				{
					watch = 0;
					EventQueue.invokeLater(new Runnable()
						{
							public void run()
							{ state.onTimer(timerType); }
						});
				}
			}
		}
		catch (InterruptedException ie)
		{/*IGNORED EXCEPTION*/}
	}
}

public void setTimer(TimerEventType type, long delay)
{
	this.timerType = type;
	if (delay == 0)
	{
		watch = 0;
	}
	else
	{
		long prev = watch;
		watch = System.currentTimeMillis() + delay;
		if (timer == null)
		{
			timer = new Thread(this);
			timer.setPriority(Thread.MIN_PRIORITY);
			timer.setDaemon(true);
			timer.start();
		}
		if ((prev == 0) || (watch < prev))
		{
			synchronized (this) { notify(); }
		}
	}
}

}