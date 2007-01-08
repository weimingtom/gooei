package de.ofahrt.gooei.lwjgl;

import gooei.utils.Icon;

import java.awt.image.BufferedImage;


final class GLIcon implements Icon
{

private final BufferedImage image;

public GLIcon(BufferedImage image)
{
	if (image == null) throw new NullPointerException();
	this.image = image;
}

public BufferedImage getImage()
{ return image; }

public int getHeight()
{ return image.getHeight(); }

public int getWidth()
{ return image.getWidth(); } 

}
