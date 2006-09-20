package thinlet;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.utils.games.Keys;

import thinlet.api.IconWidget;
import thinlet.api.MnemonicWidget;
import thinlet.help.Alignment;
import thinlet.help.Icon;
import thinlet.lwjgl.LwjglWidgetRenderer;

public class LabelWidget extends AbstractWidget implements MnemonicWidget, IconWidget
{

private int mnemonic = -1;
private String text;
private Icon icon;
private Alignment alignment = Alignment.LEFT;

private AbstractWidget forWidget = null;

public LabelWidget(ThinletDesktop desktop)
{ super(desktop); }

public int getMnemonic()
{ return mnemonic; }

public void setMnemonic(int mnemonic)
{ this.mnemonic = mnemonic; }

public String getText()
{ return text; }

public void setText(String text)
{ this.text = text; }

public Icon getIcon()
{ return icon; }

public void setIcon(Icon icon)
{ this.icon = icon; }

public Alignment getAlignment()
{ return alignment; }

public void setAlignment(Alignment alignment)
{
	if (alignment == null) throw new NullPointerException();
	this.alignment = alignment;
}

public AbstractWidget getForWidget()
{ return forWidget; }

public void setForWidget(AbstractWidget forWidget)
{ this.forWidget = forWidget; }

protected Dimension getSize(int dx, int dy)
{ return desktop.getSize(this, dx, dy); }

@Override
public Dimension getPreferredSize()
{
	if ((getWidth() > 0) && (getHeight() > 0))
		return new Dimension(getWidth(), getHeight());
	return getSize(0, 0);
}

@Override
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{
	if (!isVisible() || !isEnabled()) return false;
	if (hasMnemonic(keycode, modifiers))
	{
		AbstractWidget labelfor = getForWidget();
		if (labelfor != null)
		{
			labelfor.requestFocus();
			return true;
		}
	}
	return false;
}

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{
	Rectangle bounds = getBounds();
	renderer.paintIconAndText(this, 0, 0, bounds.width, bounds.height,
		false, false, false, false,
		0, 0, 0, 0, false, enabled ? 'e' : 'd', false);
}

}
