package de.ofahrt.utils.fonts.bmp;

import java.io.InputStream;

import de.yvert.jingle.impl.reader.ImageReader_tga;
import de.yvert.jingle.ldr.LdrImage2D;

public class Test
{

public static void main(String[] args) throws Exception
{
	InputStream in = Test.class.getClassLoader().getResourceAsStream("de/ofahrt/fonts/kevsdemo/demo.fnt");
	BmpData data = new BmpFontParser().parse(in);
	in = Test.class.getClassLoader().getResourceAsStream("de/ofahrt/fonts/kevsdemo/demo_00.tga");
	LdrImage2D image = new ImageReader_tga().load(in);
	new BmpFont(data, image);
}

}
