package thinlet.lwjgl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import thinlet.api.IconWidget;
import thinlet.api.MnemonicWidget;
import thinlet.api.Widget;
import thinlet.help.Alignment;
import thinlet.help.Icon;
import thinlet.help.TLColor;
import thinlet.help.TLFont;
import de.ofahrt.utils.fonts.FontDrawInterface;
import de.ofahrt.utils.fonts.FontTriangleInterface;
import de.ofahrt.utils.fonts.tri.TriGlyph;
import de.ofahrt.utils.fonts.tri.TriMetrics;
import de.ofahrt.utils.fonts.ttf.TtfMetrics;
import de.yvert.geometry.Vector2;

public final class LwjglWidgetRenderer
{

private static ByteBuffer byteBuffer = BufferUtils.createByteBuffer(1024);
private static DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();

private final LwjglThinletDesktop desktop;

public GLFont font;
public GLColor c_bg;
public GLColor c_text;
public GLColor c_textbg;
public GLColor c_border;
public GLColor c_disable;
public GLColor c_hover;
public GLColor c_press;
public GLColor c_focus;
public GLColor c_select;
public GLColor c_ctrl = null;

private GLFont currentFont;
private GLColor currentColor;

private int block;
//private int tx, ty;

private ClipStack clipStack = new ClipStack();

public LwjglWidgetRenderer(LwjglThinletDesktop desktop)
{
	this.desktop = desktop;
	
	setColors(0xe6e6e6, 0x000000, 0xffffff, 0x909090, 
			0xb0b0b0, 0xededed, 0xb9b9b9, 0x89899a, 0xc5c5dd);
	setDefaultFont(desktop.createFont("SansSerif", 0, 11));
	
	currentFont = font;
	currentColor = c_bg;
}

public int getBlockSize()
{ return block; }

public GLFont getDefaultFont()
{ return font; }

public void setDefaultFont(TLFont font)
{
	block = desktop.getFontMetrics(font).getHeight();
	this.font = (GLFont) font;
}

private GLColor createColor(int color)
{
	int red = (color >> 16) & 0xff;
	int green = (color >> 8) & 0xff;
	int blue = (color >> 0) & 0xff;
	return (GLColor) desktop.createColor(red, green, blue);
}

public void setColors(int background, int text, int textbackground,
		int border, int disable, int hover, int press,
		int focus, int select)
{
	c_bg = createColor(background);
	c_text = createColor(text);
	c_textbg = createColor(textbackground);
	c_border = createColor(border);
	c_disable = createColor(disable);
	c_hover = createColor(hover);
	c_press = createColor(press);
	c_focus = createColor(focus);
	c_select = createColor(select);
}

public void setCurrentFont(GLFont newfont)
{ this.currentFont = newfont; }

public GLFontMetrics getFontMetrics()
{ return currentFont.getFontMetrics(); }

public void render()
{
	GL11.glEnable(GL11.GL_CLIP_PLANE0);
	GL11.glEnable(GL11.GL_CLIP_PLANE1);
	GL11.glEnable(GL11.GL_CLIP_PLANE2);
	GL11.glEnable(GL11.GL_CLIP_PLANE3);
	
	Dimension dim = desktop.getSize();
	clipStack.init(0, 0, dim.width, dim.height);
	updateClip();
	desktop.render(this, desktop.isEnabled());
	
	GL11.glDisable(GL11.GL_CLIP_PLANE0);
	GL11.glDisable(GL11.GL_CLIP_PLANE1);
	GL11.glDisable(GL11.GL_CLIP_PLANE2);
	GL11.glDisable(GL11.GL_CLIP_PLANE3);
}


public void setColor(GLColor color)
{
	currentColor = color;
	GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
}

private void updateClip()
{
	int cx0 = clipStack.getX0();
	int cx1 = clipStack.getX1();
	int cy0 = clipStack.getY0();
	int cy1 = clipStack.getY1();
	doubleBuffer.clear();
	doubleBuffer.put(1).put(0).put(0).put(-cx0).flip();
	GL11.glClipPlane(GL11.GL_CLIP_PLANE0, doubleBuffer);
	doubleBuffer.clear();
	doubleBuffer.put(-1).put(0).put(0).put(cx1).flip();
	GL11.glClipPlane(GL11.GL_CLIP_PLANE1, doubleBuffer);
	doubleBuffer.clear();
	doubleBuffer.put(0).put(1).put(0).put(-cy0).flip();
	GL11.glClipPlane(GL11.GL_CLIP_PLANE2, doubleBuffer);
	doubleBuffer.clear();
	doubleBuffer.put(0).put(-1).put(0).put(cy1).flip();
	GL11.glClipPlane(GL11.GL_CLIP_PLANE3, doubleBuffer);
}

/** Set clip to intersection of current clip and given clip. */
public void clip(int x, int y, int width, int height)
{
	clipStack.clip(x, y, x+width, y+height);
	updateClip();
}

/** Translate all following paint operations by (dx,dy). */
public void translate(int dx, int dy)
{
	GL11.glTranslatef(dx, dy, 0);
	clipStack.translate(dx, dy);
	updateClip();
}

public void pushState()
{
	clipStack.push();
	updateClip();
}

public void popState()
{
	int ox = clipStack.getTX(), oy = clipStack.getTY();
	clipStack.pop();
	int dx = clipStack.getTX()-ox, dy = clipStack.getTY()-oy;
	if ((dx != 0) || (dy != 0))
		GL11.glTranslatef(dx, dy, 0);
	updateClip();
}

public int getClipX()
{ return clipStack.getX0(); }

public int getClipY()
{ return clipStack.getY0(); }

public int getClipWidth()
{ return clipStack.getX1()-clipStack.getX0(); }

public int getClipHeight()
{ return clipStack.getY1()-clipStack.getY0(); }

/**
 * Move the coordinate system origin to (bounds.x, bounds.y)
 * and adjust the clip according to the bounds and old clip.
 */
public boolean moveCoordSystem(Rectangle bounds)
{
	final int clipx = getClipX(), clipy = getClipY();
	final int clipwidth = getClipWidth(), clipheight = getClipHeight();
	
	// return if the component was out of the cliping rectangle
	if ((clipx + clipwidth < bounds.x) || (clipx > bounds.x + bounds.width) ||
			(clipy + clipheight < bounds.y) || (clipy > bounds.y + bounds.height))
		return false;
	
	translate(bounds.x, bounds.y);
	int nx = Math.max(0, clipx-bounds.x), ny = Math.max(0, clipy-bounds.y);
	int nw = Math.min(bounds.x+bounds.width, clipx+clipwidth)-nx-bounds.x;
	int nh = Math.min(bounds.y+bounds.height, clipy+clipheight)-ny-bounds.y;
//	System.out.println("Clip   ="+clipx+" "+clipy+" "+clipwidth+" "+clipheight);
//	System.out.println("Bounds ="+bounds.x+" "+bounds.y+" "+bounds.width+" "+bounds.height);
//	System.out.println("Newclip="+nx+" "+ny+" "+nw+" "+nh);
	clip(nx, ny, nw, nh);
	
	if (nx != getClipX()) throw new IllegalStateException(nx+" "+getClipX());
	if (ny != getClipY()) throw new IllegalStateException(ny+" "+getClipY());
	if (nw != getClipWidth()) throw new IllegalStateException(nw+" "+getClipWidth());
	if (nh != getClipHeight()) throw new IllegalStateException(nh+" "+getClipHeight());
	return true;
}

public void drawString(String s, int atx, final int aty)
{
	GLFontMetrics metrics = getFontMetrics();
	final int baseline = metrics.getDescent();
	if (metrics.metrics instanceof TtfMetrics)
	{
		((TtfMetrics) metrics.metrics).drawString(new FontDrawInterface()
			{
				public void drawPixel(int i, int j, float frac)
				{
					int x = i+1;
					int y = aty-j+baseline-1;
					GL11.glColor4f(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), frac);
					GL11.glBegin(GL11.GL_QUADS);
						GL11.glVertex2f(x, y);
						GL11.glVertex2f(x+1, y);
						GL11.glVertex2f(x+1, y+1);
						GL11.glVertex2f(x, y+1);
					GL11.glEnd();
					setColor(currentColor);
				}
			}, atx, 0, s);
	}
	else
	{
		GL11.glBegin(GL11.GL_TRIANGLES);
		((TriMetrics) metrics.metrics).drawString(new FontTriangleInterface()
			{
				public void drawTriangles(float x, float y, float scale, TriGlyph glyph)
				{
					for (int i = 0; i < glyph.triangles.length; i++)
					{
						for (int j = 0; j < 3; j++)
						{
							Vector2 v = glyph.vertices[glyph.triangles[i][j]];
							double vx = x+v.getX()*scale;
							double vy = aty+y-v.getY()*scale;
							GL11.glVertex2f((float) vx, (float) vy);
						}
					}
				}
			}, atx, 0, s);
		GL11.glEnd();
	}
}

public void drawChars(char[] chars, int off, int len, int atx, final int aty)
{
	GLFontMetrics metrics = getFontMetrics();
	final int baseline = metrics.getDescent();
	if (metrics.metrics instanceof TtfMetrics)
	{
		((TtfMetrics) metrics.metrics).drawString(new FontDrawInterface()
			{
				public void drawPixel(int i, int j, float frac)
				{
					int x = i+1;
					int y = aty-j+baseline-1;
					GL11.glColor4f(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), frac);
					GL11.glBegin(GL11.GL_QUADS);
						GL11.glVertex2f(x, y);
						GL11.glVertex2f(x+1, y);
						GL11.glVertex2f(x+1, y+1);
						GL11.glVertex2f(x, y+1);
					GL11.glEnd();
					setColor(currentColor);
				}
			}, atx, 0, chars, off, len);
	}
	else
	{
		GL11.glBegin(GL11.GL_TRIANGLES);
		((TriMetrics) metrics.metrics).drawString(new FontTriangleInterface()
			{
				public void drawTriangles(float x, float y, float scale, TriGlyph glyph)
				{
					for (int i = 0; i < glyph.triangles.length; i++)
					{
						for (int j = 0; j < 3; j++)
						{
							Vector2 v = glyph.vertices[glyph.triangles[i][j]];
							double vx = x+v.getX()*scale;
							double vy = aty+y-v.getY()*scale;
							GL11.glVertex2f((float) vx, (float) vy);
						}
					}
				}
			}, atx, 0, chars, off, len);
		GL11.glEnd();
	}
}

public void drawLine(int x0, int y0, int x1, int y1)
{
	if (x0 == x1)
	{
		if (y0 > y1)
		{
			int tmp = y0;
			y0 = y1;
			y1 = tmp;
		}
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(x0, y0);
			GL11.glVertex2f(x0, y1+1);
			GL11.glVertex2f(x0+1, y1+1);
			GL11.glVertex2f(x0+1, y0);
		GL11.glEnd();
	}
	else if (y0 == y1)
	{
		if (x0 > x1)
		{
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
		}
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(x0, y0);
			GL11.glVertex2f(x1+1, y0);
			GL11.glVertex2f(x1+1, y0+1);
			GL11.glVertex2f(x0, y0+1);
		GL11.glEnd();
	}
	// FIXME: implement
}

public void fillRect(int x, int y, int width, int height)
{
	GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x+width, y);
		GL11.glVertex2f(x+width, y+height);
		GL11.glVertex2f(x, y+height);
	GL11.glEnd();
}

public void drawRect(int x, int y, int width, int height)
{
	drawLine(x + width, y, x, y);
	drawLine(x, y, x, y + height);
	drawLine(x, y + height, x + width, y + height);
	drawLine(x + width, y + height, x + width, y);
}

private void fillGradient(int x, int y, int width, int height, boolean horizontal)
{
	setColor(c_bg);
	fillRect(x, y, width, height);
/*	if (horizontal)
	{
		if (height > block)
		{
			setColor(c_bg);
			fillRect(x, y, width, height - block);
		}
		for (int i = 0; i < width; i += block)
		{
			g.drawImage(hgradient, x + i, (height > block) ? (y + height - block) : y,
				x + Math.min(i + block, width), y + height,
				0, 0, Math.min(block, width - i), Math.min(block, height), null);
		}
	}
	else
	{
		if (width > block)
		{
			setColor(c_bg);
			fillRect(x, y, width - block, height);
		}
		for (int i = 0; i < height; i += block)
		{
			g.drawImage(vgradient, (width > block) ? (x + width - block) : x, y + i,
				x + width, y + Math.min(i + block, height),
				0, 0, Math.min(block, width), Math.min(block, height - i), null);
		}
	}*/
}

public void drawFocus(int x, int y, int width, int height)
{
	setColor(c_focus);
	int x2 = x + 1 - height % 2;
	for (int i = 0; i < width; i += 2)
	{
		fillRect(x + i, y, 1, 1);
		fillRect(x2 + i, y + height, 1, 1);
	}
	int y2 = y - width % 2;
	for (int i = 0; i < height; i += 2)
	{
		fillRect(x, y + i, 1, 1);
		fillRect(x + width, y2 + i, 1, 1);
	}
}

public void drawImage(Icon icon, int x, int y)
{
	// FIXME: implement me!
}

public void fillOval(int x, int y, int width, int height)
{
	// FIXME: implement me!
}

public void drawOval(int x, int y, int width, int height)
{
	// FIXME: implement me!
}

/** Paint a filled rectangle with optional border. */
public void paintRect(int x, int y, int width, int height,
		GLColor border, GLColor bg,
		boolean top, boolean left, boolean bottom, boolean right,
		boolean horizontal)
{
	if ((width <= 0) || (height <= 0)) return;
	if (border != null) setColor(border);
	if (top)
	{
		drawLine(x + width - 1, y, x, y);
		y++;
		height--;
		if (height <= 0) return;
	}
	if (left)
	{
		drawLine(x, y, x, y + height - 1);
		x++;
		width--;
		if (width <= 0) return;
	}
	if (bottom)
	{
		drawLine(x, y + height - 1, x + width - 1, y + height - 1);
		height--;
		if (height <= 0) return;
	}
	if (right)
	{
		drawLine(x + width - 1, y + height - 1, x + width - 1, y);
		width--;
		if (width <= 0) return;
	}
	
	if (bg == c_ctrl)
		fillGradient(x, y, width, height, horizontal);
	else
	{
		setColor(bg);
		fillRect(x, y, width, height);
	}
}

public void paintArrow(int x, int y, int width, int height, char dir)
{
	int cx = x + width / 2 - 2;
	int cy = y + height / 2 - 2;
	for (int i = 0; i < 4; i++)
	{
		if (dir == 'N') // north
			drawLine(cx + 1 - i, cy + i, cx + 1/*2*/ + i, cy + i);
		else if (dir == 'W') // west
			drawLine(cx + i, cy + 1 - i, cx + i, cy + 1/*2*/ + i);
		else if (dir == 'S') // south
			drawLine(cx + 1 - i, cy + 4 - i, cx + 1/*2*/ + i, cy + 4 - i);
		else if (dir == 'E') // east
			drawLine(cx + 4 - i, cy + 1 - i, cx + 4 - i, cy + 1/*2*/ + i);
		else
			throw new IllegalArgumentException(""+dir);
	}
}

public void paintArrow(int x, int y, int width, int height,
		char dir, boolean enabled, boolean inside, boolean pressed, String part,
		boolean top, boolean left, boolean bottom, boolean right, boolean horizontal)
{
	inside = inside && (desktop.currentMouseInteraction.insidepart == part);
	pressed = pressed && (desktop.currentMouseInteraction.pressedpart == part);
	paintRect(x, y, width, height, enabled ? c_border : c_disable,
		enabled ? ((inside != pressed) ? c_hover : (pressed ? c_press : c_ctrl)) : c_bg,
		top, left, bottom, right, horizontal);
	setColor(enabled ? c_text : c_disable);
	paintArrow(x + (left ? 1 : 0), y + (top ? 1 : 0),
		width - (left ? 1 : 0) - (right ? 1 : 0), height - (top ? 1 : 0) - (bottom ? 1 : 0), dir);
}

/** Paint component's borders and background. */
public void paintBorderAndBackground(int x, int y, int width, int height,
		boolean top, boolean left, boolean bottom, boolean right, char mode, TLColor bkg)
{
	if ((width <= 0) || (height <= 0)) return;
	
	if (top || left || bottom || right)
	{ // draw border
		setColor(((mode != 'd') && (mode != 'i')) ? c_border : c_disable);
		if (top)
		{
			drawLine(x + width - 1, y, x, y);
			y++;
			height--;
			if (height <= 0) return;
		}
		if (left)
		{
			drawLine(x, y, x, y + height - 1);
			x++;
			width--;
			if (width <= 0) return;
		}
		if (bottom)
		{
			drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			height--;
			if (height <= 0) return;
		}
		if (right)
		{
			drawLine(x + width - 1, y + height - 1, x + width - 1, y);
			width--;
			if (width <= 0) return;
		}
	}
	
	GLColor background = (GLColor) bkg;
//	GLColor background = (GLColor) component.getBackground(null);
	switch (mode)
	{
		case 'e': case 'l': case 'd': case 'g': case 'r': break;
		case 'b': case 'i': case 'x': if (background == null) background = c_bg; break;
		case 'h': background = (background != null) ? background.brighter() : c_hover; break;
		case 'p': background = (background != null) ? background.darker() : c_press; break;
		case 't': if (background == null) background = c_textbg; break;
		case 's': background = c_select; break;
		default: throw new IllegalArgumentException(""+mode);
	}
	if (((mode == 'g') || (mode == 'r')) && (background == null))
	{
		fillGradient(x, y, width, height, true);
	}
	else if (background != null)
	{
		setColor(background);
		if (mode != 'x') fillRect(x, y, width, height);
	}
}

public void paintBorderAndBackground(Widget component, int x, int y, int width, int height,
		boolean top, boolean left, boolean bottom, boolean right, char mode)
{
	GLColor background = (GLColor) ((IconWidget) component).getBackground(null);
	paintBorderAndBackground(x, y, width, height, top, left, bottom, right, mode, background);
}

/** Paint component icon and text. */
public void paintIconAndText(final IconWidget component, int x, int y, int width, int height,
		boolean top, boolean left, boolean bottom, boolean right,
		int toppadding, int leftpadding, int bottompadding, int rightpadding,
		boolean focus, char mode, boolean underline)
{
	GLColor background = (GLColor) component.getBackground(null);
	paintBorderAndBackground(x, y, width, height, top, left, bottom, right, mode, background);
	if (top) { y++; height--; }
	if (left) { x++; width--; }
	if (bottom) { height--; }
	if (right) { width--; }
	if ((width <= 0) || (height <= 0)) return;
	
	if (focus)
		drawFocus(x + 1, y + 1, width - 3, height - 3);

	String text = component.getText();
	Icon icon = component.getIcon();
	if ((text == null) && (icon == null)) return;

	x += leftpadding; y += toppadding;
	width -= leftpadding + rightpadding; height -= toppadding + bottompadding;

	Alignment alignment = component.getAlignment();
	GLFont customfont = (text != null) ? (GLFont) component.getFont(getDefaultFont()) : null;
	if (customfont != null) setCurrentFont(customfont);
	
	GLFontMetrics fm = null;
	int tw = 0, th = 0;
	int ta = 0;
	if (text != null)
	{
		fm = getFontMetrics();
		tw = fm.stringWidth(text);
		ta = fm.getAscent();
		th = fm.getDescent() + ta;
	}
	int iw = 0, ih = 0;
	if (icon != null)
	{
		iw = icon.getWidth();
		ih = icon.getHeight();
		if (text != null) iw += 2;
	}

	final boolean clipped = (tw + iw > width) || (th > height) || (ih > height);
	int cx = x;
	if (Alignment.CENTER == alignment) cx += (width - tw - iw) / 2;
	else if (Alignment.RIGHT == alignment) cx += width - tw - iw;

	if (clipped)
	{
		pushState();
		clip(x, y, width, height);
	}
	
	if (mode == 'x')
		drawLine(cx, y + height / 2, cx + iw + tw, y + height / 2);
	if (icon != null)
	{
		drawImage(icon, cx, y + (height - ih) / 2);
		cx += iw;
	}
	if (text != null)
	{
		GLColor foreground = (GLColor) component.getForeground(null);
		if (foreground == null) {
			foreground = (mode == 'l') ? GLColor.BLUE :
				(((mode != 'd') && (mode != 'r')) ? c_text : c_disable);
		}
		setColor(foreground);
		int cy = y + (height - th) / 2 + ta;
		drawString(text, cx, cy);
		if (component instanceof MnemonicWidget)
		{
			int imnemonic = ((MnemonicWidget) component).getMnemonic();
			if ((imnemonic != -1) && (imnemonic < text.length()))
			{
				int mx = cx + fm.stringWidth(text.substring(0, imnemonic));
				drawLine(mx, cy + 1, mx + fm.charWidth(text.charAt(imnemonic)), cy + 1);
			}
		}
		if (underline) // for link button
			drawLine(cx, cy + 1, cx + tw, cy + 1);
	}
	
	if (clipped)
	{
		popState();
//		setClip(clipx, clipy, clipwidth, clipheight);
	}
	
	if (customfont != null) // restore the default font
		setCurrentFont(font);
}

}
