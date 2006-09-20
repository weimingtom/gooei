package de.ofahrt.lwjgl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.GLU;

import de.ofahrt.utils.camera.Camera;
import de.yvert.geometry.Matrix4;
import de.yvert.geometry.Vector3;

/** Not threadsafe! */
public class GLTools
{

private static ByteBuffer byteBuffer = BufferUtils.createByteBuffer(1024);
private static FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

private static void setModelViewFromCamera(Camera camera)
{
	Matrix4 mat = camera.getInverseMatrix4(new Matrix4());
	floatBuffer.clear();
	for (int i = 0; i < 4; i++)
		for (int j = 0; j < 4; j++)
			floatBuffer.put((float) mat.getEntry(j, i));
	floatBuffer.flip();
	GL11.glMultMatrix(floatBuffer);
	
	Vector3 position = camera.getPosition(new Vector3());
	GL11.glTranslatef((float) -position.getV0(), (float) -position.getV1(), (float) -position.getV2());
}

public static void setCameraWithDepthOfField(Camera camera, float focus, float dx, float dy)
{
	GL11.glMatrixMode(GL11.GL_PROJECTION);
	GL11.glLoadIdentity();
	
	Matrix4 result = new Matrix4();
	double halfFovy = camera.getFieldOfViewY() /2.0;
	double coT = 1.0 / Math.tan(halfFovy*Math.PI/180.0);
	double aspect = camera.getAspectRatio();
	double farPlane = camera.getFarPlane();
	double nearPlane = camera.getNearPlane();
	result.setEntry(0, 0, coT/aspect);
	result.setEntry(1, 1, coT);
	result.setEntry(2, 2, (farPlane+nearPlane)/(nearPlane-farPlane));
	result.setEntry(2, 3, (2*farPlane*nearPlane)/(nearPlane-farPlane));
	result.setEntry(3, 2, -1);
	result.setEntry(3, 3, 0);
	
/*	double left = -nearPlane*aspect/coT - dx*nearPlane/focus;
	double right = nearPlane*aspect/coT - dx*nearPlane/focus;
	
	double bottom = -nearPlane/coT - dy * nearPlane/focus;
	double top = nearPlane/coT - dy * nearPlane/focus;
	
	double xshear = (right+left)/(right-left);
	double yshear = (top+bottom)/(top-bottom);*/
	
	double xshear = -dx*coT/(focus*aspect);
	double yshear = -dy*coT/focus;
	
	result.setEntry(0, 2, xshear);
	result.setEntry(1, 2, yshear);
	
	floatBuffer.clear();
	for (int i = 0; i < 4; i++)
		for (int j = 0; j < 4; j++)
			floatBuffer.put((float) result.getEntry(j, i));
	floatBuffer.flip();
	GL11.glMultMatrix(floatBuffer);
	
	
	GL11.glMatrixMode(GL11.GL_MODELVIEW);
	GL11.glLoadIdentity();
	GL11.glTranslatef(-dx, -dy, 0);
	setModelViewFromCamera(camera);
}

public static void setCamera(Camera camera)
{
	GL11.glMatrixMode(GL11.GL_PROJECTION);
	GL11.glLoadIdentity();
	GLU.gluPerspective((float) camera.getFieldOfViewY(),
	  (float) camera.getAspectRatio(),
	  (float) camera.getNearPlane(),
	  (float) camera.getFarPlane());
	
	GL11.glMatrixMode(GL11.GL_MODELVIEW);
	GL11.glLoadIdentity();
	setModelViewFromCamera(camera);
}

public static void setOrtho()
{
	GL11.glMatrixMode(GL11.GL_PROJECTION);
	GL11.glLoadIdentity();
	GL11.glOrtho(0, Display.getDisplayMode().getWidth()-1, 0, Display.getDisplayMode().getHeight()-1, -1, 1);
	GL11.glMatrixMode(GL11.GL_MODELVIEW);
	GL11.glLoadIdentity();
}

}
