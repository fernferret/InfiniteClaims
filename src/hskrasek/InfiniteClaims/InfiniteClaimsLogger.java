package hskrasek.InfiniteClaims;

import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;

public class InfiniteClaimsLogger 
{
	private InfiniteClaims plugin;
	private Logger log;
	
	public InfiniteClaimsLogger(String logger, InfiniteClaims plugin)
	{
		this.plugin = plugin;
		this.log = Logger.getLogger(logger);
	}
	
	private String getMessage(String msg)
	{
		PluginDescriptionFile pdFile = plugin.getDescription();
		
		return "[" + pdFile.getName() + "] " + msg;
	}
	
	public void info(String msg)
	{
		this.log.info(getMessage(msg));
	}
	
	public void warning(String msg)
	{
		this.log.warning(getMessage(msg));
	}
	
	public void severe(String msg)
	{
		this.log.severe(getMessage(msg));
	}
	
	public void debug(String msg)
	{
		this.log.log(DebugLevel.DEBUG, getMessage(msg));
	}
}
