package de.ofahrt.gooei.lwjgl;

import gooei.utils.Icon;
import de.yvert.jingle.ldr.LdrImage2D;

public class LwjglIcon implements Icon
{

private final LdrImage2D image;

public LwjglIcon(LdrImage2D image)
{
	this.image = image;
}

public LdrImage2D getImage()
{ return image; }

public int getHeight()
{ return image.getHeight(); }

public int getWidth()
{ return image.getWidth(); }

}
