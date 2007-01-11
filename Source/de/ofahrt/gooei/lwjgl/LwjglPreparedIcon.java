package de.ofahrt.gooei.lwjgl;

import gooei.utils.PreparedIcon;
import de.yvert.jingle.ldr.LdrImage2D;

public class LwjglPreparedIcon implements PreparedIcon
{

private final LdrImage2D image;
private final int id;
private boolean dirty = true;

public LwjglPreparedIcon(LdrImage2D image)
{
	this.image = image;
	this.id = OpenGLHelper.getId();
}

public LdrImage2D getImage()
{ return image; }

public int getHeight()
{ return image.getHeight(); }

public int getWidth()
{ return image.getWidth(); }

public int getId()
{ return id; }

public boolean isDirty()
{ return dirty; }

public void clearDirty()
{ dirty = false; }

}
