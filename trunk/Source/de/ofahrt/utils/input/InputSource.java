package de.ofahrt.utils.input;

public interface InputSource
{

void update();
boolean hasNext();
InputEvent next();

}
