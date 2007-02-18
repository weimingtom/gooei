package gooei.xml;

import gooei.Desktop;
import gooei.UIController;
import gooei.Widget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class NewXMLParser
{

private final Desktop desktop;
private final UIController controller;
private final WidgetFactory factory;
private final ResourceBundle bundle;

public NewXMLParser(Desktop desktop, UIController container,
		WidgetFactory factory, ResourceBundle bundle)
{
	this.desktop = desktop;
	this.controller = container;
	this.factory = factory;
	this.bundle = bundle;
}

public Widget parse(InputStream inputstream) throws IOException
{
	NewContentHandler result = new NewContentHandler(desktop, controller, factory, bundle);
	try
	{
		XMLReader xmlreader = XMLReaderFactory.createXMLReader();
		xmlreader.setContentHandler(result);
		xmlreader.parse(new InputSource(inputstream));
	}
	catch (SAXException e)
	{ throw new IOException(e); }
	return result.getRoot();
	
/*
		Object[] parentlist = null;
		Object current = null;
		StringBuffer text = new StringBuffer();
		for (int c = reader.read(); c != -1;)
		{
			if (c == '<')
				// start or standalone tag
					text.setLength(0);
					while (true)
					{ // read attributes
						boolean whitespace = false;
						while (" \t\n\r".indexOf(c) != -1)
						{ // read whitespaces
							c = reader.read();
							whitespace = true;
						}
						if (pi && (c == '?'))
						{ // end of processing instruction
							if ((c = reader.read()) != '>')
							{
								throw new IllegalArgumentException(); // read '>'
							}
						}
						else if (c == '>')
						{ // end of tag start
						}
						else if (c == '/')
						{ // standalone tag
							if ((c = reader.read()) != '>')
							{
								throw new IllegalArgumentException(); // read '>'
							}
							if (parentlist[0] == null)
							{
								reader.close();
								finishParse((Widget) current);
								return (Widget) current;
							}
							current = parentlist[0];
							parentlist = (Object[]) parentlist[1];
						}
						else if (whitespace)
						{
							while ("= \t\n\r".indexOf(c) == -1)
							{ // read to key's end
								text.append((char) c);
								c = reader.read();
							}
							String key = text.toString();
							text.setLength(0);
							while (" \t\n\r".indexOf(c) != -1) c = reader.read();
							if (c != '=') throw new IllegalArgumentException();
							while (" \t\n\r".indexOf(c = reader.read()) != -1) {}//Skip whitespace
							char quote = (char) c;
							if ((c != '\"') && (c != '\'')) throw new IllegalArgumentException();
							while (quote != (c = reader.read()))
							{
								if (c == '&')
								{
									StringBuffer eb = new StringBuffer();
									while (';' != (c = reader.read())) { eb.append((char) c); }
									String entity = eb.toString();
									if ("lt".equals(entity)) { text.append('<'); }
									else if ("gt".equals(entity)) { text.append('>'); }
									else if ("amp".equals(entity)) { text.append('&'); }
									else if ("quot".equals(entity)) { text.append('"'); }
									else if ("apos".equals(entity)) { text.append('\''); }
									else if (entity.startsWith("#"))
									{
										boolean hexa = (entity.charAt(1) == 'x');
										text.append((char) Integer.parseInt(entity.substring(hexa ? 2 : 1), hexa ? 16 : 10));
									}
									else throw new IllegalArgumentException("unknown " + "entity " + entity);
								}
								else text.append((char) c);
							}
							setAttribute(parentlist[0], current, key, text.toString());
							//'<![CDATA[' ']]>'
							text.setLength(0);
							c = reader.read();
							continue;
						}
						else throw new IllegalArgumentException();
						c = reader.read();
						break;
					}
				}
	*/
}

}
