package thinlet;

import java.awt.Rectangle;

import thinlet.api.*;
import thinlet.help.*;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.Modifiers;

public abstract class AbstractElement implements Element, ToolTipOwner
{

private String name;
private boolean enabled = true;
private boolean visible = true;

private int width = 0;
private int height = 0;

private int colspan = 1;
private int rowspan = 1;
private int weightx = 0;
private int weighty = 0;

private String tooltip = null;

private TLFont font = null;
private TLColor foreground = null;
private TLColor background = null;

private HAlignment halign = HAlignment.FILL;
private VAlignment valign = VAlignment.FILL;

protected Object parentWidget;

private Rectangle bounds;
private Rectangle port, view;
private Rectangle tooltipbounds;
private Rectangle horizontal, vertical;

private String text = null;
private Icon icon = null;
private Alignment alignment = Alignment.LEFT;

protected AbstractElement()
{/*OK*/}

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


public Object parent()
{ return parentWidget; }

public void setParent(Object parentWidget)
{ this.parentWidget = parentWidget; }

public String getName()
{ return name; }

public void setName(String name)
{ this.name = name; }

public int getWidth()
{ return width; }

public void setWidth(int width)
{ this.width = width; }

public int getHeight()
{ return height; }

public void setHeight(int height)
{ this.height = height; }

public int getColspan()
{ return colspan; }

public void setColspan(int colspan)
{ this.colspan = colspan; }

public int getRowspan()
{ return rowspan; }

public void setRowspan(int rowspan)
{ this.rowspan = rowspan; }

public int getWeightX()
{ return weightx; }

public void setWeightX(int weightx)
{ this.weightx = weightx; }

public int getWeightY()
{ return weighty; }

public void setWeightY(int weighty)
{ this.weighty = weighty; }

public boolean isEnabled()
{ return enabled; }

public void setEnabled(boolean enabled)
{ this.enabled = enabled; }

public boolean isVisible()
{ return visible; }

public void setVisible(boolean visible)
{ this.visible = visible; }

public String getToolTip()
{ return tooltip; }

public void setToolTip(String tooltip)
{ this.tooltip = tooltip; }

public TLFont getFont(TLFont def)
{ return font != null ? font : def; }

public void setFont(TLFont font)
{ this.font = font; }

public TLColor getForeground(TLColor def)
{ return foreground != null ? foreground : def; }

public void setForeground(TLColor foreground)
{ this.foreground = foreground; }

public TLColor getBackground(TLColor def)
{ return background != null ? background : def; }

public void setBackground(TLColor background)
{ this.background = background; }

public HAlignment getHalign()
{ return halign; }

public void setHalign(HAlignment halign)
{
	if (halign == null) throw new NullPointerException();
	this.halign = halign;
}

public VAlignment getValign()
{ return valign; }

public void setValign(VAlignment valign)
{
	if (valign == null) throw new NullPointerException();
	this.valign = valign;
}

private Rectangle updateRect(Rectangle rect, int x, int y, int w, int h)
{
	if (rect == null) rect = new Rectangle();
	rect.setBounds(x, y, w, h);
	return rect;
}

public Rectangle getBounds()
{ return bounds; }

public void setBounds(int x, int y, int width, int height)
{ bounds = updateRect(bounds, x, y, width, height); }

public Rectangle getPort()
{ return port; }

public void setPort(Rectangle port)
{ this.port = port; }

public void setPort(int x, int y, int width, int height)
{ port = updateRect(port, x, y, width, height); }

public Rectangle getView()
{ return view; }

public void setView(Rectangle view)
{ this.view = view; }

public void setView(int x, int y, int width, int height)
{ view = updateRect(view, x, y, width, height); }

public Rectangle getToolTipBounds()
{ return tooltipbounds; }

public void setToolTipBounds(Rectangle tooltipbounds)
{ this.tooltipbounds = tooltipbounds; }

public void setToolTipBounds(int x, int y, int width, int height)
{ tooltipbounds = updateRect(tooltipbounds, x, y, width, height); }

public Rectangle getHorizontal()
{ return horizontal; }

public void setHorizontal(Rectangle horizontal)
{ this.horizontal = horizontal; }

public void setHorizontal(int x, int y, int width, int height)
{ horizontal = updateRect(horizontal, x, y, width, height); }

public Rectangle getVertical()
{ return vertical; }

public void setVertical(Rectangle vertical)
{ this.vertical = vertical; }

public void setVertical(int x, int y, int width, int height)
{ vertical = updateRect(vertical, x, y, width, height); }


public void remove()
{
	update("validate");
	if (parent() instanceof DataWidget)
		((DataWidget) parent()).removeChild(this);
	else
		((DataElement) parent()).removeChild(this);
}

private void updateParent()
{
	if (parent() instanceof Element)
		((Element) parent()).update("validate");
	else
		((Widget) parent()).update("validate");
}

protected void update2(Widget component, String mode, int x, int y, int w, int h, boolean firstpaint)
{
	while (component != null)
	{
		if (!component.isVisible()) break;
		if ("paint".equals(mode))
		{
			Rectangle b = component.getBounds();
			if (b == null) return;
			if (firstpaint)
			{
				x = b.x;
				y = b.y;
				w = Math.abs(b.width);
				h = b.height;
				firstpaint = false;
			}
			else
			{
				x += b.x;
				y += b.y;
			}
			if (component instanceof ThinletDesktop)
				((ThinletDesktop) component).repaintDesktop(x, y, w, h);
		}
		
		ContainerWidget parent = component.parent();
		if (parent != null)
		{
			if (("layout".equals(mode)) || (("validate".equals(mode)) &&
						((parent instanceof DialogWidget) || (parent instanceof ThinletDesktop))))
			{
				Rectangle b = parent.getBounds();
				if (b == null) return;
				b.width = -1 * Math.abs(b.width);
				mode = "paint";
			}
		}
		component = parent;
	}
}

// "paint", "validate", "parent", "layout"
public void update(String mode)
{
	if ("parent".equals(mode)) updateParent();
	Element component = this;
	boolean firstpaint = true;
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	while (component != null)
	{
		if (!component.isVisible()) break;
		if ("paint".equals(mode))
		{//|| (firstpaint && (component == content))
			Rectangle b = component.getBounds();
			if (b == null) return;
			if (firstpaint)
			{
				x = b.x;
				y = b.y;
				w = Math.abs(b.width);
				h = b.height;
				firstpaint = false;
			}
			else
			{
				x += b.x;
				y += b.y;
			}
//			if (component instanceof DesktopWidget)
//				((DesktopWidget) component).repaintDesktop(x, y, w, h);
		}
		
		if (component.parent() instanceof Widget)
		{
			Widget comp = (Widget) component.parent();
			if (comp != null)
			{
				if (comp instanceof ComboBoxWidget)
					comp = ((ComboBoxWidget) comp).getComboListWidget();
				
				if (("layout".equals(mode)) || (("validate".equals(mode)) &&
							((comp instanceof ListWidget) || (comp instanceof DialogWidget) ||
							(comp instanceof ThinletDesktop))))
				{
					Rectangle b = comp.getBounds();
						if (b == null) return;
					b.width = -1 * Math.abs(b.width);
					mode = "paint";
				}
			}
			update2(comp, mode, x, y, w, h, firstpaint);
			return;
		}
		
		Element parent = (Element) component.parent();
		if (parent != null)
		{
			if (parent instanceof MenuContainerElement)
				parent = null; // parent = parent.getPopupWidget();
		}
		component = parent;
	}
}


public boolean hasMnemonic(Keys keycode, int modifiers)
{
	if (this instanceof MnemonicWidget)
	{
		MnemonicWidget mnemonic = (MnemonicWidget) this;
		if (modifiers == Modifiers.ALT)
		{
			int index = mnemonic.getMnemonic();
			if (index != -1)
			{
//				String text = mnemonic.getText();
				return (text != null) && (text.length() > index) &&
					(Character.toUpperCase(text.charAt(index)) == keycode.charRepresentation());
			}
		}
	}
	return false;
}

/** Check this widget and its children (except checked) for the given mnemonic. */
public boolean checkMnemonic(Object checked, Keys keycode, int modifiers)
{ return false; }

}
