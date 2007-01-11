package de.ofahrt.gooei.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;


public class LwjglWindow implements Runnable
{

private final int FRAMERATE = 60;

private final LwjglDesktop desktop;

public LwjglWindow(LwjglDesktop desktop)
{
	this.desktop = desktop;
}

private void initDisplay()
{
	int displayWidth = desktop.getSize().width;
	int displayHeight = desktop.getSize().height;
	int displayBPP = 24;
	try
	{
		DisplayMode modes[] = Display.getAvailableDisplayModes();
		boolean modeFound = false;
		
		for (int i = 0; i < modes.length; i++)
		{
			DisplayMode currentMode = modes[i];
			if ((currentMode.getWidth() == displayWidth) && 
			    (currentMode.getHeight() == displayHeight) && 
			    (currentMode.getBitsPerPixel() >= displayBPP))
			{
				modeFound = true;
				Display.setDisplayMode(currentMode);
				break;
			}
		}
		
		if (!modeFound)
			Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
	}
	catch (Exception e)
	{
		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		throw new RuntimeException(e);
	}
	
	Display.setTitle("Demo");
	Display.setVSyncEnabled(false);
	
	PixelFormat[] wantedFormats =
		{ new PixelFormat(0, 24, 0, 4), new PixelFormat(0, 16, 0, 0) };
	
	for (int i = 0; i < wantedFormats.length; i++)
	{
		try
		{
			Display.create(wantedFormats[i]);
			return;
		}
		catch (LWJGLException e)
		{
			System.out.println(e.getMessage());
//			if (!"Could not choose visual".equals(e.getMessage()))
//				throw new RuntimeException(e);
		}
	}
	throw new RuntimeException("Unable to find a working pixel format.");
}

protected void initGL()
{
	// initialize GL
	GL11.glShadeModel(GL11.GL_SMOOTH);
	GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	GL11.glClearDepth(1.0);
	GL11.glEnable(GL11.GL_DEPTH_TEST);
	GL11.glEnable(GL11.GL_NORMALIZE);
	
	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	GL11.glEnable(GL11.GL_BLEND);
	
	GL11.glDisable(GL11.GL_DEPTH_TEST);
	
//	GL11.glEnable(GL11.GL_LIGHTING);
//	GL11.glEnable(GL11.GL_TEXTURE_2D);
	
//	setTo(floatBuffer, ambientColor);
//	GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, floatBuffer);
//	GL11.glLightModeli(GL11.GL_LIGHT_MODEL_TWO_SIDE, 1);
	
//	setTo(floatBuffer, 1, 1, 1, 1);
//	GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, floatBuffer);
	
//	setTo(floatBuffer, 1, 1, 1, 1);
//	GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, floatBuffer);
	
//	GL11.glCullFace(GL11.GL_BACK);
//	if (fixedScene.isOneSided())
//		GL11.glEnable(GL11.GL_CULL_FACE);
//	else
//		GL11.glDisable(GL11.GL_CULL_FACE);
	
//	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
//	GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
}

private void gameLoop()
{
//	Renderer renderer = new GameRenderer(resourceManager, field);
	long lastTime = 0;
	while (true)
	{
		if (Display.isCloseRequested())
		{
			return;
		}
		else if (Display.isActive())
		{
			desktop.handleInput();
			desktop.render();
		}
		else
		{
			desktop.handleInput();
			if (Display.isVisible() || Display.isDirty())
				desktop.render();
		}
		Display.update();
		
//		Display.sync3(FRAMERATE);
		try
		{
			long time = System.currentTimeMillis();
			long oneFrame = 1000/FRAMERATE;
			boolean ok = false;
			while (time < lastTime+oneFrame)
			{
				ok = true;
				long max = oneFrame-(time-lastTime);
				if (max < 0) max = 0;
				Thread.sleep(max);
				time = System.currentTimeMillis();
			}
			if (ok) lastTime += oneFrame;
			else    lastTime = time;
		}
		catch (InterruptedException e)
		{ e.printStackTrace(); }
	}
}

public void run()
{
	initDisplay();
	initGL();
	gameLoop();
	System.exit(0);
}

}
