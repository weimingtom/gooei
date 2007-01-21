package de.ofahrt.gooei.lwjgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.EXTTextureRectangle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;
import org.lwjgl.opengl.glu.GLU;

import de.yvert.jingle.ldr.LdrImage2D;

public class OpenGLHelper
{

private static int width;
private static int height;
private static ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4*1024*1024);
//private static byte[] byteArray = new byte[4*1024*1024];
private static IntBuffer intBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

private static int nearestPower(int value)
{
	int i = 1;
	if (value == 0) return -1;
	for ( ; ; )
	{
		if (value == 1)
			return i;
		else if (value == 3)
			return i << 2;
		value >>= 1;
		i <<= 1;
	}
}

private static void toRGBAByteBuffer(LdrImage2D image, ByteBuffer target)
{
	byte[] data = image.getData();
	target.put(data);
	target.position(data.length);
}

public static int getId()
{
	intBuffer.clear();
	GL11.glGenTextures(intBuffer);
	return intBuffer.get();
}

private static void toBuffer(LdrImage2D image)
{
	width = image.getWidth();
	height = image.getHeight();
//	Color color = new Color();
	byteBuffer.clear();
	toRGBAByteBuffer(image, byteBuffer);
	byteBuffer.flip();
	
	int w = nearestPower(width);
	int h = nearestPower(height);
	if ((w != width) || (h != height))
	{
		ByteBuffer target = BufferUtils.createByteBuffer(4*1024*1024);
		GLU.gluScaleImage(GL11.GL_RGBA, width, height, GL11.GL_UNSIGNED_BYTE, byteBuffer, 
				w, h, GL11.GL_UNSIGNED_BYTE, target);
		width = w;
		height = h;
		byteBuffer = target;
		target.limit(4*w*h);
	}
}

private static void uploadBuffer(int id)
{
	GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	
//	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);
	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	
	if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic)
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 9);
	
//	long start = System.currentTimeMillis();
	int level = 0;
	while (true)
	{
//		System.out.println(level+" "+width+" x "+height);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, 4, 
				width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
		
		level++;
		if ((width == 1) || (height == 1))
		{
			if ((width == 1) && (height == 1))
				break;
			if (width == 1)
			{
				height >>= 1;
				for (int j = 0; j < height; j++)
					for (int k = 0; k < 4; k++)
					{
						int sum = 
							(byteBuffer.get(16*width*j        +k) & 0xff)+
							(byteBuffer.get(16*width*j+8*width+k) & 0xff);
						byteBuffer.put(4*j+k, (byte) ((sum+2)/4));
					}
			}
			else
			{
				width >>= 1;
		 		for (int i = 0; i < width; i++)
					for (int k = 0; k < 4; k++)
					{
						int sum = 
							(byteBuffer.get(8*i  +k) & 0xff)+
							(byteBuffer.get(8*i+4+k) & 0xff);
						byteBuffer.put(4*i+k, (byte) ((sum+2)/4));
					}
			}
		}
		else
		{
			width >>= 1;
			height >>= 1;
			
			for (int j = 0; j < height; j++)
		 		for (int i = 0; i < width; i++)
					for (int k = 0; k < 4; k++)
					{
						int sum = 
							(byteBuffer.get(16*width*j        +8*i  +k) & 0xff)+
							(byteBuffer.get(16*width*j        +8*i+4+k) & 0xff)+
							(byteBuffer.get(16*width*j+8*width+8*i  +k) & 0xff)+
							(byteBuffer.get(16*width*j+8*width+8*i+4+k) & 0xff);
						byteBuffer.put(4*width*j+4*i+k, (byte) ((sum+2)/4));
					}
		}
	}
	
//	MyGLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, 4, width,
//		height, GL11.GL_RGBA, GL11.GL_BYTE, byteBuffer);
//	long stop = System.currentTimeMillis();
//	System.out.println((stop-start)+" workin'.");
	
//	System.out.println(GL11.glGetError());
}

private static void fastUploadBuffer(int id, LdrImage2D image)
{
	byte[] data1 = image.getData().clone();
	int w = image.getWidth();
	int h = image.getHeight();
	
	GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	
	if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic)
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 9);
	
//	long start = System.currentTimeMillis();
	int level = 0;
	while (true)
	{
		byteBuffer.clear();
		byteBuffer.put(data1, 0, w*h*4);
		byteBuffer.position(w*h*4);
		byteBuffer.flip();
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, 4, 
				w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
		
		if ((w == 1) && (h == 1)) break;
		level++;
		w >>= 1;
		h >>= 1;
		
		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++)
				for (int k = 0; k < 4; k++)
				{
					int sum = 
						(data1[16*w*j    +8*i  +k] & 0xff)+
						(data1[16*w*j    +8*i+4+k] & 0xff)+
						(data1[16*w*j+8*w+8*i  +k] & 0xff)+
						(data1[16*w*j+8*w+8*i+4+k] & 0xff);
					data1[4*w*j+4*i+k] = (byte) ((sum+2)/4);
				}
	}
//	long stop = System.currentTimeMillis();
//	System.out.println((stop-start)+" workin'.");
	
//	System.out.println(GL11.glGetError());
}

private static void fastUploadBufferNoMipMaps(int id, LdrImage2D image)
{
	int w = image.getWidth();
	int h = image.getHeight();
	
	GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	
//	long start = System.currentTimeMillis();
	
	byteBuffer.clear();
	toRGBAByteBuffer(image, byteBuffer);
	byteBuffer.flip();
	
	GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 4, 
			w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
	
//	long stop = System.currentTimeMillis();
//	System.out.println((stop-start)+" workin'.");
	
//	System.out.println(GL11.glGetError());
}

public static int upload(int id, LdrImage2D image, boolean mipmaps)
{
//	long start = System.currentTimeMillis();
	if ((image.getWidth() == image.getHeight()) && 
			(nearestPower(image.getHeight()) == image.getHeight()))
	{
		if (mipmaps)
			fastUploadBuffer(id, image);
		else
			fastUploadBufferNoMipMaps(id, image);
	}
	else
	{
		toBuffer(image);
		uploadBuffer(id);
	}
//	long stop = System.currentTimeMillis();
//	System.out.println((stop-start)+" for upload.");
	return id;
}

public static int upload(LdrImage2D image, boolean mipmaps)
{ return upload(getId(), image, mipmaps); }

public static void upload(int id, LdrImage2D image)
{
	GL11.glBindTexture(EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT, id);
	byteBuffer.clear();
	toRGBAByteBuffer(image, byteBuffer);
	byteBuffer.flip();
	GL11.glTexImage2D(EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT, 0, 4, 
		image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
}

public static void display(int id, int x, int y, int w, int h)
{
	GL11.glBindTexture(EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT, id);
	GL11.glEnable(EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT);
//		GL11.glColor3f(1, 1, 1);
	GL11.glTranslatef(x, y, 0);
	GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0); GL11.glVertex2f(0, 0);
		GL11.glTexCoord2f(w, 0); GL11.glVertex2f(w, 0);
		GL11.glTexCoord2f(w, h); GL11.glVertex2f(w, h);
		GL11.glTexCoord2f(0, h); GL11.glVertex2f(0, h);
	GL11.glEnd();
	GL11.glTranslatef(-x, -y, 0);
	GL11.glDisable(EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT);
	Util.checkGLError();
}

private static int display_id;

public static void display(LdrImage2D image, int x, int y)
{
	if (display_id == 0) display_id = getId();
	int w = image.getWidth();
	int h = image.getHeight();
	if (true)
	{
		upload(display_id, image);
		display(display_id, x, y, image.getWidth(), image.getHeight());
	}
	else
	{
		upload(display_id, image, false);
		Util.checkGLError();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, display_id);
//		GL11.glColor3f(1, 1, 1);
		GL11.glTranslatef(-x, -y, 0);
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0); GL11.glVertex2f(0, 0);
			GL11.glTexCoord2f(1, 0); GL11.glVertex2f(w, 0);
			GL11.glTexCoord2f(1, 1); GL11.glVertex2f(w, h);
			GL11.glTexCoord2f(0, 1); GL11.glVertex2f(0, h);
		GL11.glEnd();
		GL11.glTranslatef(x, y, 0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Util.checkGLError();
	}
}

}
