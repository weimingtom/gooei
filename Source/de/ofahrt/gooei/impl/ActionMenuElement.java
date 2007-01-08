package de.ofahrt.gooei.impl;

import gooei.input.Keys;
import gooei.utils.MethodInvoker;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import org.lwjgl.input.Keyboard;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public class ActionMenuElement extends AbstractMenuElement
{

private int mnemonic;
private Long accelerator;
private MethodInvoker actionMethod;

public ActionMenuElement()
{/*OK*/}

public int getMnemonic()
{ return mnemonic; }

public void setMnemonic(int mnemonic)
{ this.mnemonic = mnemonic; }

public void setAction(MethodInvoker method)
{ actionMethod = method; }

public boolean invokeAction()
{
	if (actionMethod != null)
	{
		actionMethod.invoke(null);
		return true;
	}
	return false;
}

private Long parseKeystroke(String value)
{
	Long keystroke = null;
	if (value != null)
	{
		String token = value;
		try
		{
			int keycode = 0, modifiers = 0;
			StringTokenizer st = new StringTokenizer(value, " \r\n\t+");
			while (st.hasMoreTokens())
			{
				token = st.nextToken().toUpperCase();
				try
				{ modifiers = modifiers | InputEvent.class.getField(token + "_MASK").getInt(null); }
				catch (Exception exc)
				{ // not mask value
					keycode = Keyboard.class.getField("KEY_" + token).getInt(null);
				}
			}
			keystroke = new Long(((long) modifiers) << 32 | keycode);
		}
		catch (Exception exc)
		{ throw new IllegalArgumentException(token); }
	}
	return keystroke;
}

public void setAccelerator(String value)
{
	accelerator = parseKeystroke(value);
//	update(null);
}

public String getAccelerator()
{
	if (accelerator != null)
	{
		long keystroke = accelerator.longValue();
		int modifiers = (int) (keystroke >> 32);
		int keycode = (int) (keystroke & 0xffff);
		return KeyEvent.getKeyModifiersText(modifiers)+" "+KeyEvent.getKeyText(keycode);
	}
	return null;
}

boolean hasAccelerator(Keys keycode, int modifiers)
{
	if (accelerator != null)
	{
		long keystroke = accelerator.longValue();
		return ((keystroke >> 32) == modifiers) && ((keystroke & 0xffff) == keycode.charRepresentation());
	}
	return false;
}

public boolean checkMnemonic(Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	if (hasAccelerator(keycode, modifiers))
		invokeAction();
	return false;
}

@Override
public Dimension getSize(ThinletDesktop desktop, int dx, int dy)
{
	Dimension result = desktop.getSize(this, dx, dy);
	String accelText = getAccelerator(); // add accelerator width
	if (accelText != null)
	{
		//TODO font, height and gap
		result.width += 4+desktop.getFontMetrics(desktop.getDefaultFont()).stringWidth(accelText);
	}
	return result;
}

@Override
public void paint(LwjglRenderer renderer, boolean armed)
{
	Rectangle r = getBounds();
	renderer.paintIconAndText(this, r.x, r.y, r.width, r.height,
		false, false, false, false,
		2, 4, 2, 4, false,
		isEnabled() ? (armed ? 's' : 't') : 'd', false);
	String accelText = getAccelerator();
	if (accelText != null)
	{
		renderer.drawString(accelText, r.width - 2 -
			renderer.getFontMetrics().stringWidth(accelText), r.y + 2 + 10);
	}
}

}
