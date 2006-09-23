package thinlet.api;

public interface Desktop
{

SimpleClipboard getSystemClipboard();
void requestFocus();
void transferFocus();
void transferFocusBackward();

}
