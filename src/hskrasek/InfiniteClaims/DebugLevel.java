package hskrasek.InfiniteClaims;

import java.util.logging.Level;

public class DebugLevel extends Level
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1912887815265449818L;
	public static final Level DEBUG = new DebugLevel("DEBUG", Level.INFO.intValue() + 1);
	
	protected DebugLevel(final String arg0, final int arg1) 
	{
		super(arg0, arg1);
	}
	
}
