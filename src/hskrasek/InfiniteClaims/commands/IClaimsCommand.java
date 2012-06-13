package hskrasek.InfiniteClaims.commands;

import java.util.List;
import java.util.logging.Logger;

import hskrasek.InfiniteClaims.InfiniteClaims;
import hskrasek.InfiniteClaims.InfiniteClaimsLogger;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.pneumaticraft.commandhandler.Command;

public abstract class IClaimsCommand extends Command
{
	private InfiniteClaims plugin;
	private InfiniteClaimsLogger log;
	
	public IClaimsCommand(InfiniteClaims plugin) 
	{
		super(plugin);
		this.plugin = plugin;
		this.log = plugin.getLog();
	}
	
	public abstract void runCommand(CommandSender sender, List<String> args);
}
