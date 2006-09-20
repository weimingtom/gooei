package thinlet.awt;

import java.awt.*;
import java.awt.image.MemoryImageSource;

import thinlet.*;
import thinlet.help.Icon;
import thinlet.help.TLFont;

final class AWTWidgetRenderer extends AbstractWidgetRenderer
{

private static int evm = 0;

private final AWTWidgetEnvironment env;
private final WidgetContainer thinlet;
private final UIState state;

private AWTFont font;
private AWTColor c_bg;
private AWTColor c_text;
private AWTColor c_textbg;
private AWTColor c_border;
private AWTColor c_disable;
private AWTColor c_hover;
private AWTColor c_press;
private AWTColor c_focus;
private AWTColor c_select;
private AWTColor c_ctrl = null;

private int block;
private Image hgradient, vgradient;

public AWTWidgetRenderer(AWTWidgetEnvironment env, WidgetContainer thinlet)
{
	this.env = env;
	this.thinlet = thinlet;
	this.state = thinlet.getState();
	setColors(0xe6e6e6, 0x000000, 0xffffff, 0x909090, 
			0xb0b0b0, 0xededed, 0xb9b9b9, 0x89899a, 0xc5c5dd);
	setFont(new AWTFont(new Font("SansSerif", Font.PLAIN, 12)));
}

public int getBlockSize()
{ return block; }

public AWTFont getFont()
{ return font; }

public void setFont(TLFont font)
{
	block = env.getFontMetrics(font).getHeight();
	this.font = (AWTFont) font;
	hgradient = vgradient = null;
}

public void setColors(int background, int text, int textbackground,
		int border, int disable, int hover, int press,
		int focus, int select)
{
	c_bg = new AWTColor(background);
	c_text = new AWTColor(text);
	c_textbg = new AWTColor(textbackground);
	c_border = new AWTColor(border);
	c_disable = new AWTColor(disable);
	c_hover = new AWTColor(hover);
	c_press = new AWTColor(press);
	c_focus = new AWTColor(focus);
	c_select = new AWTColor(select);
	hgradient = vgradient = null;
}

public void paint(Graphics g)
	{
		g.setFont(font.getFont());
		if (hgradient == null) {
			int[][] pix = new int[2][block * block];
			int r1 = (int) (255*c_bg.getRed()), r2 = (int) (255*c_press.getRed());
			int g1 = (int) (255*c_bg.getGreen()), g2 = (int) (255*c_press.getGreen());
			int b1 = (int) (255*c_bg.getBlue()), b2 = (int) (255*c_press.getBlue());
			for (int i = 0; i < block; i++)
			{
				int cr = r1 - (r1 - r2) * i / block;
				int cg = g1 - (g1 - g2) * i / block;
				int cb = b1 - (b1 - b2) * i / block;
				int color = (255 << 24) | (cr << 16) | (cg << 8) | cb;
				for (int j = 0; j < block; j++) {
					pix[0][i * block + j] = color;
					pix[1][j * block + i] = color;
				}
			}
			hgradient = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(block, block, pix[0], 0, block));
			vgradient = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(block, block, pix[1], 0, block));
		}
		//g.setColor(Color.orange);
		//g.fillRect(0, 0, getSize().width, getSize().height);
		//long time = System.currentTimeMillis();
		Rectangle clip = g.getClipBounds();
		///dg.setClip(r.x, r.y, r.width, r.height);
		paint(g, clip.x, clip.y, clip.width, clip.height, thinlet.getDesktop(), env.isEnabled());
		//System.out.println(System.currentTimeMillis() - time);
		//g.setClip(0, 0, getSize().width, getSize().height);
		//g.setColor(Color.red); g.drawRect(clip.x, clip.y, clip.width - 1, clip.height - 1);
	}

/**
 * @param clipx the cliping rectangle is relative to the component's
 * parent location similar to the component's bounds rectangle
 * @param clipy
 * @param clipwidth
 * @param clipheight
 * @throws java.lang.IllegalArgumentException
 */
public void paint(Graphics g,
	int clipx, int clipy, int clipwidth, int clipheight,
	Widget component, boolean enabled)
{
	if (!component.getBoolean("visible")) { return; }
	Rectangle bounds = component.getRectangle("bounds");
	if (bounds == null) { return; }
	// negative component width indicates invalid component layout
	if (bounds.width < 0)
	{
		bounds.width = Math.abs(bounds.width);
		component.doLayout();
	}
	// return if the component was out of the cliping rectangle
	if ((clipx + clipwidth < bounds.x) ||
			(clipx > bounds.x + bounds.width) ||
			(clipy + clipheight < bounds.y) ||
			(clipy > bounds.y + bounds.height)) {
		return;
	}
	// set the clip rectangle relative to the component location
	clipx -= bounds.x; clipy -= bounds.y;
	g.translate(bounds.x, bounds.y); 
	//g.setClip(0, 0, bounds.width, bounds.height);
	String classname = component.getClassName();
	boolean pressed = (state.mousepressed == component);
	boolean inside = (state.mouseinside == component) &&
		((state.mousepressed == null) || pressed);
	boolean focus = state.focusinside && (state.focusowner == component);
	enabled = component.getBoolean("enabled"); //enabled &&

	if ("label" == classname)
	{
		paint(component, 0, 0, bounds.width, bounds.height,
				g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
				0, 0, 0, 0, false, enabled ? 'e' : 'd', "left", true, false);
	}
	else if (("button" == classname) || ("togglebutton" == classname))
	{
		boolean toggled = ("togglebutton" == classname) && component.getBoolean("selected");
		boolean link = ("button" == classname) && (component.getChoice("type") == "link");
			if (link) {
				paint(component, 0, 0, bounds.width, bounds.height,
					g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
					0, 0, 0, 0, focus, enabled ? (pressed ? 'e' : 'l') : 'd', "center",
					true, enabled && (inside != pressed));
			} else { // disabled toggled
				char mode = enabled ? ((inside != pressed) ? 'h' : ((pressed || toggled) ? 'p' : 'g')) : 'd';
				paint(component, 0, 0, bounds.width, bounds.height,
					g, clipx, clipy, clipwidth, clipheight, true, true, true, true,
					2, 5, 2, 5, focus, mode, "center", true, false);
				//(enabled && ("button" == classname) && get(component, "type") == "default")...
			}
		}
		else if ("checkbox" == classname) {
			paint(component, 0, 0, bounds.width, bounds.height,
				g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
				0, block + 3, 0, 0, false, enabled ? 'e' : 'd', "left", true, false);

			boolean selected = component.getBoolean("selected");
			String group = component.getString("group");
			AWTColor border = enabled ? c_border : c_disable;
			AWTColor foreground = enabled ? ((inside != pressed) ? c_hover :
				(pressed ? c_press : c_ctrl)) : c_bg;
			int dy = (bounds.height - block + 2) / 2;
			if (group == null) {
				paintRect(g, 1, dy + 1, block - 2, block - 2,
					border, foreground, true, true, true, true, true);
			} else {
				g.setColor(((foreground != c_ctrl) ? foreground : c_bg).getColor());
				g.fillOval(1, dy + 1, block - 3 + evm, block - 3 + evm);
				g.setColor(border.getColor());
				g.drawOval(1, dy + 1, block - 3, block - 3);
			}
			if (focus) {
				drawFocus(g, 0, 0, bounds.width - 1, bounds.height - 1);
			}
			if((!selected && inside && pressed) ||
					(selected && (!inside || !pressed))) {
				g.setColor((enabled ? c_text : c_disable).getColor());
				if (group == null) {
					g.fillRect(3, dy + block - 9, 2 + evm, 6 + evm);
					g.drawLine(3, dy + block - 4, block - 4, dy + 3);
					g.drawLine(4, dy + block - 4, block - 4, dy + 4);
				} else {
					g.fillOval(5, dy + 5, block - 10 + evm, block - 10 + evm);
					g.drawOval(4, dy + 4, block - 9, block - 9);
				}
			}
		}
		else if ("combobox" == classname) {
			if (component.getBoolean("editable")) {
				Icon icon = component.getIcon();
				int left = (icon != null) ? icon.getWidth() : 0;
				paintField(g, clipx, clipy, clipwidth, clipheight, component,
					bounds.width - block, bounds.height, focus, enabled, false, left);
				if (icon != null) {
					g.drawImage(icon.getImage(), 2, (bounds.height - icon.getHeight()) / 2, null);
				}
				paintArrow(g, bounds.width - block, 0, block, bounds.height,
					'S', enabled, inside, pressed, "down", true, false, true, true, true);
			} else {
				paint(component, 0, 0, bounds.width, bounds.height,
					g, clipx, clipy, clipwidth, clipheight,
					true, true, true, true, 1, 1, 1, 1 + block, focus,
					enabled ? ((inside != pressed) ? 'h' : (pressed ? 'p' : 'g')) : 'd',
					"left", false, false);
				g.setColor((enabled ? c_text : c_disable).getColor());
				paintArrow(g, bounds.width - block, 0, block, bounds.height, 'S');
			}
		}
		else if (":combolist" == classname) {
			paintScroll(component, classname, pressed, inside, focus, false, enabled,
				g, clipx, clipy, clipwidth, clipheight);
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds.width, bounds.height, focus, enabled, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			paintScroll(component, classname, pressed, inside, focus, true, enabled,
				g, clipx, clipy, clipwidth, clipheight);
		}
		else if ("tabbedpane" == classname) {
			int i = 0; Widget selectedtab = null;
			int selected = component.getInteger("selected");
			String placement = component.getChoice("placement");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
			boolean stacked = (placement == "stacked");
			int bx = stacked ? 0 : horizontal ? 2 : 1, by = stacked ? 0 : horizontal ? 1 : 2,
				bw = 2 * bx, bh = 2 * by;
			// paint tabs except the selected one
			int pcx = clipx, pcy = clipy, pcw = clipwidth, pch = clipheight;
			clipx = Math.max(0, clipx); clipy = Math.max(0, clipy);
			clipwidth = Math.min(bounds.width, pcx + pcw) - clipx;
			clipheight = Math.min(bounds.height, pcy + pch) - clipy;
			g.clipRect(clipx, clipy, clipwidth, clipheight); // intersection of clip and bound
			for (Widget tab = component.component(); tab != null; tab = tab.next())
			{
				Rectangle r = tab.getRectangle("bounds");
				if (selected != i) {
					boolean hover = inside && (state.mousepressed == null) && (state.insidepart == tab);
					boolean tabenabled = enabled && tab.getBoolean("enabled");
					paint(tab, r.x + bx, r.y + by, r.width - bw, r.height - bh,
						g, clipx, clipy, clipwidth, clipheight,
						(placement != "bottom"), (placement != "right"),
						!stacked && (placement != "top"), (placement != "left"),
						1, 3, 1, 3, false, tabenabled ? (hover ? 'h' : 'g') : 'd', "left", true, false);
				} else {
					selectedtab = tab;
					// paint tabbedpane border
					paint(tab, (placement == "left") ? r.width - 1 : 0,
						stacked ? (r.y + r.height - 1) : (placement == "top") ? r.height - 1 : 0,
						(horizontal || stacked) ? bounds.width : (bounds.width - r.width + 1),
						stacked ? (bounds.height - r.y - r.height + 1) :
						horizontal ? (bounds.height - r.height + 1) : bounds.height,
						g, true, true, true, true, enabled ? 'e' : 'd');
					Widget comp = selectedtab.component();
					if ((comp != null) && comp.getBoolean("visible")) {
						clipx -= r.x; clipy -= r.y; g.translate(r.x, r.y); // relative to tab
						paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
						clipx += r.x; clipy += r.y; g.translate(-r.x, -r.y);
					}
				}
				i++;
			}
			
			// paint selected tab and its content
			if (selectedtab != null) {
				Rectangle r = selectedtab.getRectangle("bounds");
				// paint selected tab
				int ph = stacked ? 3 : (horizontal ? 5 : 4);
				int pv = stacked ? 1 : (horizontal ? 2 : 3);
				paint(selectedtab, r.x, r.y, r.width, r.height,
					g, clipx, clipy, clipwidth, clipheight,
					(placement != "bottom"), (placement != "right"),
					!stacked && (placement != "top"), (placement != "left"),
					pv, ph, pv, ph, focus, enabled ? 'b' : 'i', "left", true, false);
			}
			g.setClip(pcx, pcy, pcw, pch);
		}
		else if (("panel" == classname) || ("dialog" == classname)) {
			int titleheight = component.getIntegerImpl(":titleheight", 0);
			if ("dialog" == classname) {
				paint(component, 0, 0, bounds.width, 3 + titleheight,
					g, clipx, clipy, clipwidth, clipheight, true, true, false, true,
					1, 2, 1, 2, false, 'g', "left", false, false);
				int controlx = bounds.width - titleheight - 1;
				if (component.getBoolean("closable")) {
					paint(component, g, controlx, 3, titleheight - 2, titleheight - 2, 'c');
					controlx -= titleheight;
				}
				if (component.getBoolean("maximizable")) {
					paint(component, g, controlx, 3, titleheight - 2, titleheight - 2, 'm');
					controlx -= titleheight;
				}
				if (component.getBoolean("iconifiable")) {
					paint(component, g, controlx, 3, titleheight - 2, titleheight - 2, 'i');
				}
				paintRect(g, 0, 3 + titleheight, bounds.width, bounds.height - 3 - titleheight,
					c_border, c_press, false, true, true, true, true); // lower part excluding titlebar
				paint(component, // content area
					3, 3 + titleheight, bounds.width - 6, bounds.height - 6 - titleheight,
					g, true, true, true, true, 'b');
			} else { // panel
				boolean border = component.getBoolean("border");
				paint(component, 0, titleheight / 2, bounds.width, bounds.height - (titleheight / 2),
					g, border, border, border, border, enabled ? 'e' : 'd');
				paint(component, 0, 0, bounds.width, titleheight, // panel title
					g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
					0, 3, 0, 3, false, enabled ? 'x' : 'd', "left", false, false);
			}
			
			if (component.getRectangle(":port") != null) {
				paintScroll(component, classname, pressed, inside, focus, false, enabled,
					g, clipx, clipy, clipwidth, clipheight);
			}
			else {
				for (Widget comp = component.component(); comp != null; comp = comp.next())
				{
					paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
				}
			}
		}
		else if ("desktop" == classname) {
			paintRect(g, 0, 0, bounds.width, bounds.height,
				c_border, c_bg, false, false, false, false, true);
			paintReverse(g, clipx, clipy, clipwidth, clipheight,
				component.component(), enabled);
			//g.setColor(Color.red); if (clip != null) g.drawRect(clipx, clipy, clipwidth, clipheight);
			if ((state.tooltipowner != null) && (component == thinlet.getDesktop())) {
				Rectangle r = state.tooltipowner.getRectangle(":tooltipbounds");
				paintRect(g, r.x, r.y, r.width, r.height,
					c_border, c_bg, true, true, true, true, true);
				String text = state.tooltipowner.getString("tooltip");
				g.setColor(c_text.getColor());
				g.drawString(text, r.x + 2, r.y + g.getFontMetrics().getAscent() + 2); //+nullpointerexception
			}			
		}
		else if ("spinbox" == classname) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds.width - block, bounds.height, focus, enabled, false, 0);
			paintArrow(g, bounds.width - block, 0, block, bounds.height / 2,
					'N', enabled, inside, pressed, "up", true, false, false, true, true);
			paintArrow(g, bounds.width - block, bounds.height / 2,
				block, bounds.height - (bounds.height / 2),
				'S', enabled, inside, pressed, "down", true, false, true, true, true);
		}
		else if ("progressbar" == classname) {
			int minimum = component.getInteger("minimum");
			int maximum = component.getInteger("maximum");
			int value = component.getInteger("value");
			// fixed by by Mike Hartshorn and Timothy Stack
			boolean horizontal = ("vertical" != component.getChoice("orientation"));
			int length = (value - minimum) *
				((horizontal ? bounds.width : bounds.height) - 1) / (maximum - minimum);
			paintRect(g, 0, 0, horizontal ? length : bounds.width,
				horizontal ? bounds.height : length, enabled ? c_border : c_disable,
				c_select, true, true, horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? (bounds.width - length) : bounds.width	,
				horizontal ? bounds.height : (bounds.height - length),
				enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
		}
		else if ("slider" == classname) {
			if (focus) {
				drawFocus(g, 0, 0, bounds.width - 1, bounds.height - 1);
			}
			int minimum = component.getInteger("minimum");
			int maximum = component.getInteger("maximum");
			int value = component.getInteger("value");
			boolean horizontal = ("vertical" != component.getChoice("orientation"));
			int length = (value - minimum) *
				((horizontal ? bounds.width : bounds.height) - block) /
				(maximum - minimum);
			paintRect(g, horizontal ? 0 : 3, horizontal ? 3 : 0,
				horizontal ? length : (bounds.width - 6),
				horizontal ? (bounds.height - 6) : length,
				enabled ? c_border : c_disable,
				c_bg, true, true, horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? block : bounds.width, horizontal ? bounds.height : block,
				enabled ? c_border : c_disable,
				enabled ? c_ctrl : c_bg, true, true, true, true, true);
			paintRect(g, horizontal ? (block + length) : 3,
				horizontal ? 3 : (block + length),
				bounds.width - (horizontal ? (block + length) : 6),
				bounds.height - (horizontal ? 6 : (block + length)),
				enabled ? c_border : c_disable,
				c_bg, horizontal, !horizontal, true, true, true);
		}
		else if ("splitpane" == classname)
		{
			boolean horizontal = ("vertical" != component.getChoice("orientation"));
			int divider = component.getInteger("divider");
			paintRect(g, horizontal ? divider : 0, horizontal ? 0 : divider,
				horizontal ? 5 : bounds.width, horizontal ? bounds.height : 5,
				c_border, c_bg, false, false, false, false, true);
			if (focus) {
				if (horizontal) { drawFocus(g, divider, 0, 4, bounds.height - 1); }
				else { drawFocus(g, 0, divider, bounds.width - 1, 4); }
			}
			g.setColor((enabled ? c_border : c_disable).getColor());
			int xy = horizontal ? bounds.height : bounds.width;
			int xy1 = Math.max(0, xy / 2 - 12);
			int xy2 = Math.min(xy / 2 + 12, xy - 1);
			for (int i = divider + 1; i < divider + 4; i += 2) {
				if (horizontal) { g.drawLine(i, xy1, i, xy2); }
					else { g.drawLine(xy1, i, xy2, i); }
			}
			Widget comp1 = component.component();
			if (comp1 != null) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp1, enabled);
				Widget comp2 = comp1.next();
				if (comp2 != null) {
					paint(g, clipx, clipy, clipwidth, clipheight, comp2, enabled);
				}
			}
		}
		else if (("list" == classname) ||
				("table" == classname) || ("tree" == classname))
		{
			paintScroll(component, classname, pressed, inside, focus,
				focus && (component.component() == null), enabled,
				g, clipx, clipy, clipwidth, clipheight);
		}
		else if ("separator" == classname) {
			g.setColor((enabled ? c_border : c_disable).getColor());
			g.fillRect(0, 0, bounds.width + evm, bounds.height + evm);
		}
		else if ("menubar" == classname)
		{
			Widget selected = component.getSelectedWidget();
			int lastx = 0;
			for (Widget menu = component.component(); menu != null; menu = menu.next())
			{
				Rectangle mb = menu.getRectangle("bounds");
				if (clipx + clipwidth <= mb.x) { break; }
				if (clipx >= mb.x + mb.width) { continue; }
				boolean menuenabled = enabled && menu.getBoolean("enabled");
				boolean armed = (selected == menu);
				boolean hoover = (selected == null) && (state.insidepart == menu);
				paint(menu, mb.x, 0, mb.width, bounds.height,
					g, clipx, clipy, clipwidth, clipheight, // TODO disabled
					armed, armed, true, armed, 1, 3, 1, 3, false,
					enabled ? (menuenabled ? (armed ? 's' : (hoover ? 'h' : 'g')) : 'r') : 'd', "left", true, false);
				lastx = mb.x + mb.width;
			}
			paintRect(g, lastx, 0, bounds.width - lastx, bounds.height,
				enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg,
				false, false, true, false, true);
		}
		else if (":popup" == classname) {
			paintRect(g, 0, 0, bounds.width, bounds.height,
				c_border, c_textbg, true, true, true, true, true);
			Widget selected = component.getSelectedWidget();
			for (Widget menu = component.getMenu().component(); menu != null; menu = menu.next())
			{
				Rectangle r = menu.getRectangle("bounds");
				if (clipy + clipheight <= r.y) break;
				if (clipy >= r.y + r.height) continue;
				String itemclass = menu.getClassName();
				if (itemclass == "separator") {
					g.setColor(c_border.getColor());
					g.fillRect(r.x, r.y, bounds.width - 2 + evm, r.height + evm);
				} else {
					boolean armed = (selected == menu);
					boolean menuenabled = menu.getBoolean("enabled");
					paint(menu, r.x, r.y, bounds.width - 2, r.height,
						g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
						2, (itemclass == "checkboxmenuitem") ? (block + 7) : 4, 2, 4, false,
						menuenabled ? (armed ? 's' : 't') : 'd', "left", true, false);
					if (itemclass == "checkboxmenuitem") {
						boolean checked = menu.getBoolean("selected");
						String group = menu.getString("group");
						g.translate(r.x + 4, r.y + 2);
						g.setColor((menuenabled ? c_border : c_disable).getColor());
						if (group == null) {
							g.drawRect(1, 1, block - 3, block - 3);
						} else {
							g.drawOval(1, 1, block - 3, block - 3);
						}
						if (checked) {
							g.setColor((menuenabled ? c_text : c_disable).getColor());
							if (group == null) {
								g.fillRect(3, block - 9, 2 + evm, 6 + evm);
								g.drawLine(3, block - 4, block - 4, 3);
								g.drawLine(4, block - 4, block - 4, 4);
							} else {
								g.fillOval(5, 5, block - 10 + evm, block - 10 + evm);
								g.drawOval(4, 4, block - 9, block - 9);
							}
						}
						g.translate(-r.x - 4, -r.y - 2);
					}
					if (itemclass == "menu") {
						paintArrow(g, r.x + bounds.width - block, r.y, block, r.height, 'E');
					}
					else if (menu instanceof MenuItemWidget)
					{
						MenuItemWidget miwidget = (MenuItemWidget) menu;
						String accelerator = miwidget.getAccelerator();
						if (accelerator != null)
						{ //TODO
							g.drawString(accelerator, bounds.width - 4 -
								thinlet.getFontMetrics(font).stringWidth(accelerator), r.y + 2 + 10);
						}
					}
				}
			}
		}
		else throw new IllegalArgumentException(classname);
		g.translate(-bounds.x, -bounds.y);
		clipx += bounds.x; clipy += bounds.y;
	}

	private void paintReverse(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight,
			Widget component, boolean enabled) {
		if (component != null) {
			Rectangle bounds = component.getRectangle("bounds");
			if ((clipx < bounds.x) ||
					(clipx + clipwidth > bounds.x + bounds.width) ||
					(clipy < bounds.y) ||
					(clipy + clipheight > bounds.y + bounds.height)) {
				paintReverse(g, clipx, clipy, clipwidth, clipheight,
					component.next(), enabled);
			}
			paint(g, clipx, clipy, clipwidth, clipheight, component, enabled);
		}
	}
	
	private void paintField(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight, Widget component,
			int width, int height,
			boolean focus, boolean enabled, boolean hidden, int left) {
		boolean editable = component.getBoolean("editable");
		paintRect(g, 0, 0, width, height, enabled ? c_border : c_disable,
			editable ? (AWTColor) component.getColor("background", c_textbg) : c_bg,
			true, true, true, true, true);
		g.clipRect(1 + left, 1, width - left - 2, height - 2);
		
		String text = component.getString("text");
		int offset = component.getIntegerImpl(":offset", 0);
		AWTFont currentfont = (AWTFont) component.getFont();
		g.setFont(currentfont.getFont());
		FontMetrics fm = g.getFontMetrics();

		int caret = 0;
		if (focus) { 
			int start = component.getInteger("start"); 
			int end = component.getInteger("end");
			caret = hidden ? (fm.charWidth('*') * end) :
				fm.stringWidth(text.substring(0, end));
			if (start != end) {
				int is = hidden ? (fm.charWidth('*') * start) :
					fm.stringWidth(text.substring(0, start));
				g.setColor(c_select.getColor());
				g.fillRect(2 + left - offset + Math.min(is, caret), 1,
					Math.abs(caret - is) + evm, height - 2 + evm);
			}
		}

		if (focus) { // draw caret
			g.setColor(c_focus.getColor());
			g.fillRect(1 + left - offset + caret, 1, 1 + evm, height - 2 + evm);
		}

		g.setColor((enabled ? (AWTColor) component.getColor("foreground", c_text) : c_disable).getColor());
		int fx = 2 + left - offset;
		int fy = (height + fm.getAscent() - fm.getDescent()) / 2;
		if (hidden) {
			int fh = fm.charWidth('*');
			for (int i = text.length(); i > 0; i--) {
				g.drawString("*", fx, fy);
				fx += fh;
			}
		} else {
			g.drawString(text, fx, fy);
		}
		if (currentfont != null) { g.setFont(font.getFont()); }
		g.setClip(clipx, clipy, clipwidth, clipheight);

		if (focus) { // draw dotted rectangle
			drawFocus(g, 1, 1, width - 3, height - 3);
		}
	}
	
	/**
	 * @param component scrollable widget
	 * @param classname
	 * @param pressed
	 * @param inside
	 * @param focus
	 * @param enabled
	 * @param g grahics context
	 * @param clipx current cliping x location relative to the component
	 * @param clipy y location of the cliping area relative to the component
	 * @param clipwidth width of the cliping area
	 * @param clipheight height of the cliping area
	 * @param header column height
	 * @param topborder bordered on the top if true
	 * @param border define left, bottom, and right border if true
	 */
	private void paintScroll(Widget component, String classname, boolean pressed,
			boolean inside, boolean focus, boolean drawfocus, boolean enabled,
			Graphics g, int clipx, int clipy, int clipwidth, int clipheight) {
		Rectangle port = component.getRectangle(":port");
		Rectangle horizontal = component.getRectangle(":horizontal");
		Rectangle vertical = component.getRectangle(":vertical");
		Rectangle view = component.getRectangle(":view");
		
		if (horizontal != null) { // paint horizontal scrollbar
			int x = horizontal.x; int y = horizontal.y; int width = horizontal.width; int height = horizontal.height;
			paintArrow(g, x, y, block, height,
				'W', enabled, inside, pressed, "left", true, true, true, false, true);
			paintArrow(g, x + width - block, y, block, height,
				'E', enabled, inside, pressed, "right", true, false, true, true, true);
				
			int track = width - (2 * block);
			if (track < 10) {
				paintRect(g, x + block, y, track, height,
					enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
			}
			else {
				int knob = Math.max(track * port.width / view.width, 10);
				int decrease = view.x * (track - knob) / (view.width - port.width);
				paintRect(g, x + block, y, decrease, height,
					enabled ? c_border : c_disable, c_bg, false, true, true, false, true);
				paintRect(g, x + block + decrease, y, knob, height,
					enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, true, true, true, true, true);
				int n = Math.min(5, (knob - 4) / 3);
				g.setColor((enabled ? c_border : c_disable).getColor());
				int cx = (x + block + decrease) + (knob + 2 - n * 3) / 2;
				for (int i = 0; i < n; i++ ) {
					g.drawLine(cx + i * 3, y + 3, cx + i * 3, y + height - 5);
				}
				int increase = track - decrease - knob;
				paintRect(g, x + block + decrease + knob, y, increase, height,
					enabled ? c_border : c_disable, c_bg, false, false, true, true, true);
			}
		}
			
		if (vertical != null) { // paint vertical scrollbar
			int x = vertical.x; int y = vertical.y; int width = vertical.width; int height = vertical.height;
			paintArrow(g, x, y, width, block,
				'N', enabled, inside, pressed, "up", true, true, false, true, false);
			paintArrow(g, x, y + height - block, width, block,
				'S', enabled, inside, pressed, "down", false, true, true, true, false);
				
			int track = height - (2 * block);
			if (track < 10) {
				paintRect(g, x, y + block, width, track,
					enabled ? c_border : c_disable, c_bg, true, true, true, true, false);
			}
			else {
				int knob = Math.max(track * port.height / view.height, 10);
				int decrease = view.y * (track - knob) / (view.height - port.height);
				paintRect(g, x, y + block, width, decrease,
					enabled ? c_border : c_disable, c_bg, true, false, false, true, false);
				paintRect(g, x, y + block + decrease, width, knob,
					enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, true, true, true, true, false);
				int n = Math.min(5, (knob - 4) / 3);
				g.setColor((enabled ? c_border : c_disable).getColor());
				int cy = (y + block + decrease) + (knob + 2 - n * 3) / 2;
				for (int i = 0; i < n; i++ ) {
					g.drawLine(x + 3, cy + i * 3, x + width - 5, cy + i * 3);
				}
				int increase = track - decrease - knob;
				paintRect(g, x, y + block + decrease + knob, width, increase,
					enabled ? c_border : c_disable, c_bg, false, false, true, true, false);
			}
		}
		
		boolean hneed = (horizontal != null); boolean vneed = (vertical != null);
		if (("panel" != classname) && ("dialog" != classname) &&
				(("textarea" != classname) || component.getBoolean("border")))
		{
			paintRect(g, port.x - 1, port.y - 1, port.width + (vneed ? 1 : 2), port.height + (hneed ? 1 : 2),
				enabled ? c_border : c_disable, (AWTColor) component.getColor("background", c_textbg),
				true, true, !hneed, !vneed, true); // TODO not editable textarea background color
			if ("table" == classname)
			{
				Widget header = component.getHeader();
				if (header != null)
				{
					int[] columnwidths = (int []) component.getInternal(":widths");
					Widget column = header.component();
					int x = 0;
					g.clipRect(0, 0, port.width + 2, port.y); // not 2 and decrease clip area...
					for (int i = 0; i < columnwidths.length; i++) {
						if (i != 0) { column = column.next(); }
						boolean lastcolumn = (i == columnwidths.length - 1);
						int width = lastcolumn ? (view.width - x + 2) : columnwidths[i];
						
						paint(column, x - view.x, 0, width, port.y - 1,
							g, clipx, clipy, clipwidth, clipheight,
							true, true, false, lastcolumn, 1, 1, 0, 0, false,
							enabled ? 'g' : 'd', "left", false, false);
						
						Object sort = column.getChoice("sort"); // "none", "ascent", "descent"
						if (sort != null) {
							paintArrow(g, x - view.x + width - block, 0, block, port.y,
								(sort == "ascent") ? 'S' : 'N');
						}
						x += width;
					}
					g.setClip(clipx, clipy, clipwidth, clipheight);
				}
			}
		}
		int x1 = Math.max(clipx, port.x);
		int x2 = Math.min(clipx + clipwidth, port.x + port.width);
		int y1 = Math.max(clipy, port.y);
		int y2 = Math.min(clipy + clipheight, port.y + port.height);
		if ((x2 > x1) && (y2 > y1)) {
			g.clipRect(x1, y1, x2 - x1, y2 - y1);
			g.translate(port.x - view.x, port.y - view.y);
			
			paint(component, classname, focus, enabled,
				g, view.x - port.x + x1, view.y - port.y + y1, x2 - x1, y2 - y1, port.width, view.width);
			
			g.translate(view.x - port.x, view.y - port.y);
			g.setClip(clipx, clipy, clipwidth, clipheight);
		}
		if (focus && drawfocus) { // draw dotted rectangle around the viewport
			drawFocus(g, port.x, port.y, port.width - 1, port.height - 1);
		}
	}
	
	/**
	 * Paint scrollable content
	 * @param component a panel
	 */
	private void paint(Widget component,
			String classname, boolean focus, boolean enabled,
			Graphics g, int clipx, int clipy, int clipwidth, int clipheight,
			int portwidth, int viewwidth)
	{
		if ("textarea" == classname) {
			char[] chars = (char[]) component.getInternal(":text");
			int start = focus ? component.getInteger("start") : 0;
			int end = focus ? component.getInteger("end") : 0;
			int is = Math.min(start, end); int ie = Math.max(start, end);
			AWTFont customfont = getFont();
			g.setFont(customfont.getFont());
			FontMetrics fm = g.getFontMetrics();
			int fontascent = fm.getAscent(); int fontheight = fm.getHeight();
			int ascent = 1;
			
			AWTColor textcolor = enabled ? (AWTColor) component.getColor("foreground", c_text) : c_disable;
			for (int i = 0, j = 0; j <= chars.length; j++) {
				if ((j == chars.length) || (chars[j] == '\n')) {
					if (clipy + clipheight <= ascent) { break; } // the next lines are bellow paint rectangle
					if (clipy < ascent + fontheight) { // this line is not above painting area
						if (focus && (is != ie) && (ie >= i) && (is <= j)) {
							int xs = (is < i) ? -1 : ((is > j) ? (viewwidth - 1) :
								fm.charsWidth(chars, i, is - i));
							int xe = ((j != -1) && (ie > j)) ? (viewwidth - 1) :
								fm.charsWidth(chars, i, ie - i);
							g.setColor(c_select.getColor());
							g.fillRect(1 + xs, ascent, xe - xs + evm, fontheight + evm);
						}
						g.setColor(textcolor.getColor());
						g.drawChars(chars, i, j - i, 1, ascent + fontascent);
						if (focus && (end >= i) && (end <= j)) {
							int caret = fm.charsWidth(chars, i, end - i);
							g.setColor(c_focus.getColor());
							g.fillRect(caret, ascent, 1 + evm, fontheight + evm);
						}
					}
					ascent += fontheight;
					i = j + 1;
				}
			}
			if (customfont != null) { g.setFont(font.getFont()); } //restore the default font
		}
		else if (":combolist" == classname)
		{
			ComboListWidget combolist = (ComboListWidget) component;
			Widget lead = combolist.getLead();
			for (Widget choice = combolist.getComboBoxWidget().component();
					choice != null; choice = choice.next())
			{
				Rectangle r = choice.getRectangle("bounds");
				if (clipy + clipheight <= r.y) break;
				if (clipy >= r.y + r.height) continue;
				paint(choice, r.x, r.y, portwidth, r.height,
					g, clipx, clipy, clipwidth, clipheight,
					false, false, false, false, 2, 4, 2, 4, false,
					choice.getBoolean("enabled") ? ((lead == choice) ? 's' : 't') : 'd',
					"left", false, false);
			}
		}
		else if (("panel" == classname) || ("dialog" == classname)) {
			for (Widget comp = component.component(); comp != null; comp = comp.next())
			{
				paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
			}
		}
		else { //if (("list" == classname) || ("table" == classname) || ("tree" == classname))
			Widget lead = component.getLead();
			int[] columnwidths = ("table" == classname) ? ((int []) component.getInternal(":widths")) : null;
			boolean line = component.getBoolean("line"); int iline = line ? 1 : 0;
			boolean angle = ("tree" == classname) && component.getBoolean("angle");
			for (Widget item = component.component(), next = null; item != null; item = next)
			{
				if (focus && (lead == null)) {
					component.setWidget(":lead", lead = item); // draw first item focused when lead is null
				}
				Rectangle r = item.getRectangle("bounds");
				if (clipy + clipheight <= r.y) { break; } // clip rectangle is above
				boolean subnode = false; boolean expanded = false;
				if ("tree" != classname) {
					next = item.next();
				}
				else {
					subnode = (next = item.component()) != null;
					expanded = subnode && item.getBoolean("expanded");
					if (!expanded) {
						for (Widget node = item; (node != component) &&
							((next = node.next()) == null); node = node.parent())
						{/*DO NOTHING*/}
					}
				}
				if (clipy >= r.y + r.height + iline) {
					if (angle)
					{ // TODO draw dashed line
						Widget nodebelow = item.next();
						if (nodebelow != null)
						{ // and the next node is bellow clipy
							g.setColor(c_bg.getColor());
							int x = r.x - block / 2;
							g.drawLine(x, r.y, x, nodebelow.getRectangle("bounds").y);
						}
					}
					continue; // clip rectangle is bellow
				}
				
				boolean selected = item.getBoolean("selected");
				paintRect(g, ("tree" != classname) ? 0 : r.x, r.y,
					("tree" != classname) ? viewwidth : r.width, r.height, null,
					selected ? c_select : c_textbg, false, false, false, false, true);
				if (focus && (lead == item)) { // focused
					drawFocus(g, ("tree" != classname) ? 0 : r.x, r.y,
						(("tree" != classname) ? viewwidth : r.width) - 1, r.height - 1);
				}
				if (line) {
					g.setColor(c_bg.getColor());
					g.drawLine(0, r.y + r.height, viewwidth, r.y + r.height);
				}
				if ("table" != classname) { // list or tree
					boolean itemenabled = enabled && item.getBoolean("enabled");
					paint(item, r.x, r.y, viewwidth, r.height,
						g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
						1, 3, 1, 3, false, itemenabled ? 'e' : 'd', "left", false, false);
					if ("tree" == classname) {
						int x = r.x - block / 2; int y = r.y + (r.height - 1) / 2;
						if (angle) {
							g.setColor(c_bg.getColor());
							g.drawLine(x, r.y, x, y); g.drawLine(x, y, r.x - 1, y);
							Widget nodebelow = item.next();
							if (nodebelow != null) {
								g.drawLine(x, y, x, nodebelow.getRectangle("bounds").y);
							}
						}
						if (subnode) {
							paintRect(g, x - 4, y - 4, 9, 9, itemenabled ? c_border : c_disable,
								itemenabled ? c_ctrl : c_bg, true, true, true, true, true);
							g.setColor((itemenabled ? c_text : c_disable).getColor());
							g.drawLine(x - 2, y, x + 2, y);
							if (!expanded) { g.drawLine(x, y - 2, x, y + 2); }
						}
					}
				}
				else { // table
					int i = 0; int x = 0;
					for (Widget cell = item.component(); cell != null; cell = cell.next())
					{
						if (clipx + clipwidth <= x) { break; }
						//column width is defined by header calculated in layout, otherwise is 80
						int iwidth = 80;
						if ((columnwidths != null) && (columnwidths.length > i)) {
							iwidth = (i != columnwidths.length - 1) ?
								columnwidths[i] : Math.max(iwidth, viewwidth - x);
						}
						if (clipx < x + iwidth) {
							boolean cellenabled = enabled && cell.getBoolean("enabled");
							paint(cell, r.x + x, r.y, iwidth, r.height - 1,
								g, clipx, clipy, clipwidth, clipheight, false, false, false, false,
								1, 1, 1, 1, false, cellenabled ? 'e' : 'd', "left", false, false);
						}
						i++; x += iwidth;
					}
				}
			}
		}
	}

	private void paintRect(Graphics g, int x, int y, int width, int height,
			AWTColor border, AWTColor bg,
			boolean top, boolean left, boolean bottom, boolean right, boolean horizontal)
	{
		if ((width <= 0) || (height <= 0)) return;
		if (border != null)
			g.setColor(border.getColor());
		if (top) {
			g.drawLine(x + width - 1, y, x, y);
			y++; height--; if (height <= 0) return;
		}
		if (left) {
			g.drawLine(x, y, x, y + height - 1);
			x++; width--; if (width <= 0) return;
		}
		if (bottom) {
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			height--; if (height <= 0) return;
		}
		if (right) {
			g.drawLine(x + width - 1, y + height - 1, x + width - 1, y);
			width--; if (width <= 0) return;
		}

		if (bg == c_ctrl) {
			fill(g, x, y, width, height, horizontal);
		}
		else {
			g.setColor(bg.getColor());
			g.fillRect(x, y, width + evm, height + evm);
		}
	}
	
	/**
	 * Fill the given rectangle with gradient
	 */
	private void fill(Graphics g, int x, int y, int width, int height, boolean horizontal)
	{
		if (horizontal) {
			if (height > block) {
				g.setColor(c_bg.getColor());
				g.fillRect(x, y, width + evm, height - block + evm);
			}
			for (int i = 0; i < width; i += block) {
				g.drawImage(hgradient, x + i, (height > block) ? (y + height - block) : y,
					x + Math.min(i + block, width) + evm, y + height + evm,
					0, 0, Math.min(block, width - i) + evm, Math.min(block, height) + evm, null);
			}
		}
		else {
			if (width > block) {
				g.setColor(c_bg.getColor());
				g.fillRect(x, y, width - block + evm, height + evm);
			}
			for (int i = 0; i < height; i += block) {
				g.drawImage(vgradient, (width > block) ? (x + width - block) : x, y + i,
					x + width + evm, y + Math.min(i + block, height) + evm,
					0, 0, Math.min(block, width) + evm, Math.min(block, height - i) + evm, null);
			}
		}
	}

	private void paintArrow(Graphics g, int x, int y, int width, int height,
			char dir, boolean enabled, boolean inside, boolean pressed, String part,
			boolean top, boolean left, boolean bottom, boolean right, boolean horizontal)
	{
		inside = inside && (state.insidepart == part);
		pressed = pressed && (state.pressedpart == part);
		paintRect(g, x, y, width, height, enabled ? c_border : c_disable,
			enabled ? ((inside != pressed) ? c_hover :
				(pressed ? c_press : c_ctrl)) : c_bg,
			top, left, bottom, right, horizontal);
		g.setColor((enabled ? c_text : c_disable).getColor());
		paintArrow(g, x + (left ? 1 : 0), y + (top ? 1 : 0),
			width - (left ? 1 : 0) - (right ? 1 : 0), height - (top ? 1 : 0) - (bottom ? 1 : 0), dir);
	}

	private void paintArrow(Graphics g,
			int x, int y, int width, int height, char dir)
	{
		int cx = x + width / 2 - 2;
		int cy = y + height / 2 - 2;
		for (int i = 0; i < 4; i++) {
			if (dir == 'N') { // north
				g.drawLine(cx + 1 - i, cy + i, cx + 1/*2*/ + i, cy + i);
			}
			else if (dir == 'W') { // west
				g.drawLine(cx + i, cy + 1 - i, cx + i, cy + 1/*2*/ + i);
			}
			else if (dir == 'S') { // south
				g.drawLine(cx + 1 - i, cy + 4 - i, cx + 1/*2*/ + i, cy + 4 - i);
			}
			else { // east
				g.drawLine(cx + 4 - i, cy + 1 - i, cx + 4 - i, cy + 1/*2*/ + i);
			}
		}
	}
	
	/**
	 * Paint component's borders and background
	 */
	private void paint(Widget component, int x, int y, int width, int height,
			Graphics g, boolean top, boolean left, boolean bottom, boolean right,
			char mode)
	{
		if ((width <= 0) || (height <= 0)) { return; }
	
		if (top || left || bottom || right) { // draw border
			g.setColor((((mode != 'd') && (mode != 'i')) ? c_border : c_disable).getColor());
			if (top) {
				g.drawLine(x + width - 1, y, x, y);
				y++; height--; if (height <= 0) { return; }
			}
			if (left) {
				g.drawLine(x, y, x, y + height - 1);
				x++; width--; if (width <= 0) { return; }
			}
			if (bottom) {
				g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
				height--; if (height <= 0) { return; }
			}
			if (right) {
				g.drawLine(x + width - 1, y + height - 1, x + width - 1, y);
				width--; if (width <= 0) { return; }
			}
		}
	
		AWTColor background = (AWTColor) component.getColor("background", null);
		switch (mode) {
			case 'e': case 'l': case 'd': case 'g': case 'r': break;
			case 'b': case 'i': case 'x': if (background == null) { background = c_bg; } break;
			case 'h': background = (background != null) ? background.brighter() : c_hover; break;
			case 'p': background = (background != null) ? background.darker() : c_press; break;
			case 't': if (background == null) { background = c_textbg; } break;
			case 's': background = c_select; break;
			default: throw new IllegalArgumentException();
		}
		if (((mode == 'g') || (mode == 'r')) && (background == null)) {
			fill(g, x, y, width, height, true);
		}
		else if (background != null) {
			g.setColor(background.getColor());
			if (mode != 'x') { g.fillRect(x, y, width + evm, height + evm); } 
		}
	}

	private void paint(Widget component, Graphics g,
			int x, int y, int width, int height, char type)
	{
		paint(component, x, y, width, height, g, true, true, true, true, 'g');
		g.setColor(Color.black);
		switch (type) {
			case 'c': // closable dialog button
				g.drawLine(x + 3, y + 4, x + width - 5, y + height - 4);
				g.drawLine(x + 3, y + 3, x + width - 4, y + height - 4);
				g.drawLine(x + 4, y + 3, x + width - 4, y + height - 5);
				g.drawLine(x + width - 5, y + 3, x + 3, y + height - 5);
				g.drawLine(x + width - 4, y + 3, x + 3, y + height - 4);
				g.drawLine(x + width - 4, y + 4, x + 4, y + height - 4);
				break;
			case 'm': // maximizable dialog button
				g.drawRect(x + 3, y + 3, width - 7, height - 7);
				g.drawLine(x + 4, y + 4, x + width - 5, y + 4);
				break;
			case 'i': // iconifiable dialog button
				g.fillRect(x + 3, y + height - 5, width - 6, 2);
				break;
		}
	}

	/**
	 * Paint component icon and text (using default or custom font)
	 * @param mnemonic find mnemonic index and underline text
	 */
	private void paint(Widget component, int x, int y, int width, int height,
			Graphics g, int clipx, int clipy, int clipwidth, int clipheight,
			boolean top, boolean left, boolean bottom, boolean right,
			int toppadding, int leftpadding, int bottompadding, int rightpadding, boolean focus,
			char mode, String alignment, boolean mnemonic, boolean underline)
	{
		paint(component, x, y, width, height,
			g, top, left, bottom, right, mode);
		if (top) { y++; height--; } if (left) { x++; width--; }
		if (bottom) { height--; } if (right) { width--; }
		if ((width <= 0) || (height <= 0)) { return; }
		
		if (focus) {
			drawFocus(g, x + 1, y + 1, width - 3, height - 3);
		}

		String text = component.getString("text");
		Icon icon = component.getIcon();
		if ((text == null) && (icon == null)) { return; }
	
		x += leftpadding; y += toppadding;
		width -= leftpadding + rightpadding; height -= toppadding + bottompadding;

		alignment = component.getChoice("alignment");
		AWTFont customfont = (text != null) ? (AWTFont) component.getFont() : null;
		if (customfont != null) { g.setFont(customfont.getFont()); }

		FontMetrics fm = null;
		int tw = 0, th = 0;
		int ta = 0;
		if (text != null) {
			fm = g.getFontMetrics();
			tw = fm.stringWidth(text);
			ta = fm.getAscent();
			th = fm.getDescent() + ta;
		}
		int iw = 0, ih = 0;
		if (icon != null) {
			iw = icon.getWidth();
			ih = icon.getHeight();
			if (text != null) { iw += 2; }
		}

		boolean clipped = (tw + iw > width) || (th > height) || (ih > height);
		int cx = x;
		if ("center" == alignment) { cx += (width - tw - iw) / 2; }
			else if ("right" == alignment) { cx += width - tw - iw; }

		if (clipped) { g.clipRect(x, y, width, height); }
		if (mode == 'x') { g.drawLine(cx, y + height / 2, cx + iw + tw, y + height / 2); }
		if (icon != null) {
			g.drawImage(icon.getImage(), cx, y + (height - ih) / 2, null);
			cx += iw;
		}
		if (text != null) { 
			AWTColor foreground = (AWTColor) component.getColor("foreground", null);
			if (foreground == null) {
				foreground = (mode == 'l') ? AWTColor.BLUE :
					(((mode != 'd') && (mode != 'r')) ? c_text : c_disable);
			}
			g.setColor(foreground.getColor());
			int ty = y + (height - th) / 2 + ta;
			g.drawString(text, cx, ty);
			if (mnemonic) {
				int imnemonic = component.getInteger("mnemonic");
				if ((imnemonic != -1) && (imnemonic < text.length())) {
					int mx = cx + fm.stringWidth(text.substring(0, imnemonic));
					g.drawLine(mx, ty + 1, mx + fm.charWidth(text.charAt(imnemonic)), ty + 1);
				}
			}
			if (underline) { // for link button
				g.drawLine(cx, ty + 1, cx + tw, ty + 1);
			}
		}
		if (clipped) { g.setClip(clipx, clipy, clipwidth, clipheight); }
		
		if (customfont != null) { g.setFont(font.getFont()); } //restore the default font
	}

private void drawFocus(Graphics g, int x, int y, int width, int height)
{
	g.setColor(c_focus.getColor());
	int x2 = x + 1 - height % 2;
	for (int i = 0; i <= width; i += 2)
	{
		g.fillRect(x + i, y, 1, 1); g.fillRect(x2 + i, y + height, 1, 1);
	}
	int y2 = y - width % 2;
	for (int i = 2; i <= height; i += 2)
	{
		g.fillRect(x, y + i, 1, 1); g.fillRect(x + width, y2 + i, 1, 1);
	}
}

}
