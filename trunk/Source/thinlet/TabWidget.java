package thinlet;

import java.awt.Dimension;

import thinlet.api.IconWidget;
import thinlet.api.MnemonicWidget;
import thinlet.api.Widget;
import thinlet.help.Alignment;
import thinlet.help.Icon;
import thinlet.lwjgl.LwjglWidgetRenderer;

public final class TabWidget extends AbstractContainerWidget<Widget> implements MnemonicWidget, IconWidget
{

private String text = null;
private Icon icon = null;
private Alignment alignment = Alignment.LEFT;
private int mnemonic = -1;

public TabWidget(ThinletDesktop desktop)
{ super(desktop); }

public Widget getContent()
{ return getChild(0); }

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

public int getMnemonic()
{ return mnemonic; }

public void setMnemonic(int mnemonic)
{ this.mnemonic = mnemonic; }

public Dimension getSize(int dx, int dy)
{ return desktop.getSize(this, dx, dy); }

@Override
public boolean acceptChild(Widget child)
{ return !(child instanceof DialogWidget); }

@Override
public void paint(LwjglWidgetRenderer renderer, boolean enabled)
{ throw new UnsupportedOperationException(); }

}
