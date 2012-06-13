package hskrasek.InfiniteClaims.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import hskrasek.InfiniteClaims.InfiniteClaims;

public class HelpCommand extends IClaimsCommand
{
	InfiniteClaims plugin;
	
	public HelpCommand(InfiniteClaims plugin) 
	{
		super(plugin);
		this.plugin = plugin;
		this.setName("IClaims Help");
		this.setCommandUsage("/iclaims");
		this.setArgRange(0, 1);
		this.addKey("iclaims");
		this.addKey("iclaims help");
		this.addCommandExample("/iclaims help");
		this.setPermission("infiniteclaims.help", "Displays help menu", PermissionDefault.TRUE);
	}

	
	public void runCommand(CommandSender sender, List<String> args) 
	{
		String message = plugin.getPluginPrefix() + "Available commands:\n" +
				"     " + ChatColor.YELLOW + "/iclaims plot" + ChatColor.WHITE + "- Takes you to your plot\n"
				+ "     " + ChatColor.YELLOW + "/iclaims reset" + ChatColor.WHITE + "- Resets your plot\n"
				+ "     " + ChatColor.YELLOW + "/iclaims addmember" + ChatColor.WHITE + "- Adds a member to your plot\n"
				+ "     " + ChatColor.YELLOW + "/iclaims removemember" + ChatColor.WHITE + "- Removes a member from your plot\n";
				sender.sendMessage(message);
		plugin.log.debug("Ran command");
	}	
}
