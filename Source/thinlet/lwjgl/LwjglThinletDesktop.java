package thinlet.lwjgl;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import thinlet.ThinletDesktop;
import thinlet.help.*;
import de.ofahrt.lwjgl.GLTools;
import de.ofahrt.lwjgl.LwjglInputSource;
import de.ofahrt.utils.fonts.tri.TriData;
import de.ofahrt.utils.fonts.tri.TriFont;
import de.ofahrt.utils.fonts.ttf.TtFont;
import de.ofahrt.utils.fonts.ttf.TtfData;
import de.ofahrt.utils.input.InputEvent;
import de.ofahrt.utils.input.InputEventType;
import de.ofahrt.utils.input.KeyboardEvent;
import de.ofahrt.utils.input.MouseEvent;
import de.yvert.camera.CameraTools;
import de.yvert.camera.Plane;
import de.yvert.camera.TargetCamera;
import de.yvert.geometry.Ray;
import de.yvert.geometry.Vector3;

public class LwjglThinletDesktop extends ThinletDesktop
{

protected final boolean useCamera = false;
protected final boolean useTriFont = true;

protected int offsetX = 0;
protected int offsetY = 0;
protected final Dimension size = new Dimension();
protected final LwjglWidgetRenderer renderer;
protected final TargetCamera camera;

private final LwjglInputSource inputSource = new LwjglInputSource();

public LwjglThinletDesktop()
{
	super();
	renderer = new LwjglWidgetRenderer(this);
	camera = new TargetCamera(new Vector3(260, 260, 0), 300);
}

@Override
public void setTimer(TimerEventType type, long delay)
{ // FIXME: Implement!
}

@Override
public TLFont getDefaultFont()
{ return renderer.getDefaultFont(); }

@Override
public TLFontMetrics getFontMetrics(TLFont font)
{ return font.getFontMetrics(); }

@Override
public TLFont createFont(String name, int style, int fontSize)
{
	try
	{
		if (useTriFont)
			return new GLFont(new TriFont(new TriData(TtfData.load(name)), fontSize));
		else
			return new GLFont(new TtFont(TtfData.load(name), fontSize));
	}
	catch (IOException e)
	{ throw new RuntimeException(name, e); }
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
public Clipboard getSystemClipboard()
{ return Toolkit.getDefaultToolkit().getSystemClipboard(); }

@Override
public void requestDesktopFocus()
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
public void repaintDesktop(int tx, int ty, int width, int height)
{/*Do nothing!*/}

@Override
public Icon loadIcon(String path)
{ return null; }




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
	setPosition(0, 0);
	setSize(640, 480);
	LwjglWindow window = new LwjglWindow(this);
	new Thread(window).start();
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
