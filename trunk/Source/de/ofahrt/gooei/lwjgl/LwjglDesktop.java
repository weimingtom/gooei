package de.ofahrt.gooei.lwjgl;

import gooei.SimpleClipboard;
import gooei.font.DefaultFontRegistry;
import gooei.font.Font;
import gooei.font.FontMetrics;
import gooei.font.FontRegistry;
import gooei.input.InputEvent;
import gooei.input.InputEventType;
import gooei.input.KeyboardEvent;
import gooei.input.MouseEvent;
import gooei.utils.Icon;
import gooei.utils.PreparedIcon;
import gooei.utils.TLColor;
import gooei.utils.TimerEventType;
import gooei.xml.WidgetFactory;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import de.ofahrt.gooei.awt.AWTClipboard;
import de.yvert.camera.CameraTools;
import de.yvert.camera.TargetCamera;
import de.yvert.geometry.Plane;
import de.yvert.geometry.Ray;
import de.yvert.geometry.Vector3;
import de.yvert.jingle.ImageReader;
import de.yvert.jingle.ReaderWriterFactory;
import de.yvert.jingle.ldr.LdrImage2D;

public final class LwjglDesktop extends AbstractDesktop
{

private final boolean useCamera = false;

protected final FontRegistry fontRegistry;
protected int offsetX = 0;
protected int offsetY = 0;
protected final Dimension size = new Dimension();
protected final LwjglRenderer renderer;
protected final TargetCamera camera;

private final LwjglInputSource inputSource = new LwjglInputSource();

public LwjglDesktop(FontRegistry fontRegistry, WidgetFactory factory, int width, int height)
{
	super(factory);
	this.fontRegistry = fontRegistry;
	renderer = new LwjglRenderer(this, fontRegistry.getDefaultFont());
	camera = new TargetCamera(new Vector3(260, 260, 0), 300);
	
	setPosition(0, 0);
	setSize(width, height);
}

public LwjglDesktop(FontRegistry fontRegistry, int width, int height)
{ this(fontRegistry, new LwjglWidgetFactory(), width, height); }

public LwjglDesktop(FontRegistry fontRegistry)
{ this(fontRegistry, new LwjglWidgetFactory(), 640, 480); }

public LwjglDesktop(WidgetFactory factory, int width, int height)
{ this(new DefaultFontRegistry(), factory, width, height); }

public LwjglDesktop(WidgetFactory factory)
{ this(new DefaultFontRegistry(), factory, 640, 480); }

public LwjglDesktop()
{ this(new DefaultFontRegistry(), new LwjglWidgetFactory(), 640, 480); }

@Override
public void setTimer(TimerEventType type, long delay)
{ // FIXME: Implement!
}

@Override
public Font getDefaultFont()
{ return renderer.getDefaultFont(); }

@Override
public FontMetrics getFontMetrics(Font font)
{ return font.getMetrics(); }

public FontRegistry getFontRegistry()
{ return fontRegistry; }

public void addFont(String name, Font font)
{
	if (!(fontRegistry instanceof DefaultFontRegistry)) throw new UnsupportedOperationException();
	if (renderer.getDefaultFont() == null)
		renderer.setDefaultFont(font);
	((DefaultFontRegistry) fontRegistry).addFont(name, font);
}

public GLColor createColor(float red, float green, float blue, float alpha)
{ return new GLColor(red, green, blue, alpha); }

@Override
public TLColor createColor(int red, int green, int blue)
{ return createColor(red/255.0f, green/255.0f, blue/255.0f, 1); }

@Override
public TLColor createColor(int red, int green, int blue, int alpha)
{ return createColor(red/255.0f, green/255.0f, blue/255.0f, alpha/255.0f); }

@Override
public int getBlockSize()
{ return renderer.getBlockSize(); }

@Override
public void show()
{ showContainer(); }

@Override
public SimpleClipboard getSystemClipboard()
{ return new AWTClipboard(Toolkit.getDefaultToolkit().getSystemClipboard()); }

@Override
public void requestFocus()
{ // FIXME: Implement!
}

@Override
public void transferFocus()
{ // FIXME: Implement!
}

@Override
public void transferFocusBackward()
{ // FIXME: Implement!
}

@Override
public boolean isDesktopEnabled()
{ return true; }

@Override
public Dimension getSize()
{ return size; }

@Override
public void setCursor(Cursor cursor)
{ // FIXME: Implement!
}

@Override
public void repaintDesktop(int tx, int ty, int w, int h)
{/*Do nothing!*/}

@Override
public Icon loadIcon(String filename)
{
	try
	{
		InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
		if (in == null) throw new NullPointerException(filename);
		int i = filename.lastIndexOf('.');
		String extension = filename.substring(i+1);
		ImageReader reader = ReaderWriterFactory.getReader(extension);
		LdrImage2D image = (LdrImage2D) reader.load(in);
		return new LwjglIcon(image);
	}
	catch (IOException e)
	{ e.printStackTrace(); }
	catch (RuntimeException e)
	{ e.printStackTrace(); }
	return null;
}

@Override
public PreparedIcon prepareIcon(Icon icon)
{
	LwjglIcon i = (LwjglIcon) icon;
	return new LwjglPreparedIcon(i.getImage());
}




public void setPosition(int x, int y)
{
	offsetX = x;
	offsetY = y;
}

public void setSize(int width, int height)
{
	camera.setSize(width, height);
	size.width = width;
	size.height = height;
	onResize();
	onFocusGained();
}

public void showContainer()
{
	LwjglWindow window = new LwjglWindow(this);
	window.run();
}

protected int mx, my;

protected void convertPosition(int x, int y)
{
	if (!useCamera)
	{
		mx = x;
		my = y;
	}
	else
	{
		Ray ray = new Ray();
		CameraTools.getRay(camera, ray, x, y);
		Plane p = new Plane(Vector3.ZERO, Vector3.Z);
		Vector3 isec = p.intersect(ray, new Vector3());
		if (isec != null)
		{
			mx = (int) Math.floor(isec.getX()+0.5f);
			my = (int) Math.floor(isec.getY()+0.5f);
		}
	}
	
	mx = mx-offsetX;
	my = -my+size.height+offsetY;
}

protected void convertPosition(MouseEvent event)
{
	int nx = event.getX();
	int ny = event.getY();
	if (useCamera)
	{
		Ray ray = new Ray();
		CameraTools.getRay(camera, ray, nx, ny);
		Plane p = new Plane(Vector3.ZERO, Vector3.Z);
		Vector3 isec = p.intersect(ray, new Vector3());
		if (isec != null)
		{
			nx = (int) Math.floor(isec.getX()+0.5f);
			ny = (int) Math.floor(isec.getY()+0.5f);
		}
	}
	nx = nx-offsetX;
	ny = -ny+size.height+offsetY;
	event.translate(nx-event.getX(), ny-event.getY());
}

public void handleEvent(InputEvent event)
{
	if (event instanceof MouseEvent)
	{
		MouseEvent mouseevent = (MouseEvent) event;
		convertPosition(mouseevent);
		onMouse(mouseevent);
	}
	else if (event instanceof KeyboardEvent)
	{
		KeyboardEvent keyevent = (KeyboardEvent) event;
		if (keyevent.getType() == InputEventType.KEY_DOWN)
			onKey(keyevent, false);
	}
}

public void handleInput()
{
	if (useCamera)
	{
		if (Mouse.isButtonDown(1))
		{
			camera.rotateLeft(Mouse.getDX()/2.0);
			camera.rotateUp(-Mouse.getDY()/2.0);
		}
		camera.zoomIn(Mouse.getDWheel()/10.0);
	}
	
	inputSource.update();
	while (inputSource.hasNext())
	{
		InputEvent event = inputSource.next();
		handleEvent(event);
	}
}

public void render2()
{
	if (useCamera)
		GLTools.setCamera(camera);
	else
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, size.width, 0, size.height, -1, 1);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	GL11.glEnable(GL11.GL_BLEND);
	
	GL11.glScalef(1, -1, 1);
	GL11.glTranslatef(0, -Display.getDisplayMode().getHeight(), 0);
	GL11.glTranslatef(offsetX, -offsetY, 0);
	
	renderer.render();
}

public void render()
{
	GL11.glClearColor(0, 0, 0, 0);
	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	render2();
}

}
