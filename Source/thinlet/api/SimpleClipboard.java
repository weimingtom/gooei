package thinlet.api;

import java.io.IOException;

public interface SimpleClipboard
{

void copy(String s) throws IOException;
String get() throws IOException;

}
