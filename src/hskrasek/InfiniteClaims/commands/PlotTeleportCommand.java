package hskrasek.InfiniteClaims.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.pneumaticraft.commandhandler.CommandHandler;

import hskrasek.InfiniteClaims.InfiniteClaims;
import hskrasek.InfiniteClaims.InfiniteClaimsUtilities;

public class PlotTeleportCommand extends IClaimsCommand
{
	InfiniteClaims plugin;
	InfiniteClaimsUtilities icUtils;
	
	public PlotTeleportCommand(InfiniteClaims plugin) 
	{
		super(plugin);
		this.plugin = plugin;
		this.icUtils = plugin.getIcUtils();
		this.setName("Plot Teleportation");
		this.setCommandUsage(String.format("%s/iclaims plot %s{NAME} %sp:%s[PLAYER]%s w:%s[WORLD]", 
				ChatColor.YELLOW, ChatColor.RED, ChatColor.YELLOW, ChatColor.WHITE, ChatColor.YELLOW, ChatColor.WHITE));
		this.setArgRange(1, 3);
		this.addKey("myplot");
		this.addKey("iplot");
		this.addKey("iclaimsplot");
		Permission perm = new Permission("iclaims.tp.self", "InfiniteClaims Plot Teleportation", PermissionDefault.OP);
		this.setPermission(perm);
		this.addCommandExample(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot1");
		this.addCommandExample(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot1 " + ChatColor.YELLOW + "p:" + ChatColor.WHITE + "HeroSteve");
		this.addCommandExample(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot1 " + ChatColor.YELLOW + "w:" + ChatColor.WHITE + "DonatorCreative");
		this.addCommandExample(ChatColor.YELLOW + "/iclaims plot " + ChatColor.RED + "plot1 " + ChatColor.YELLOW + "p:" + ChatColor.WHITE + 
				"HeroSteve" + ChatColor.YELLOW + "w:" + ChatColor.WHITE + "DonatorCreative");
		plugin.getLog().debug("PlotTeleportCommand registered");
	}

	public void runCommand(CommandSender sender, List<String> args) 
	{
		plugin.log.debug("Running command");
		String plotName = args.get(0);
		String playerName = CommandHandler.getFlag("p:", args);
		String worldName = CommandHandler.getFlag("w:", args);
		
		Player player = null;
		
		if(sender instanceof Player)
		{
			player = (Player)sender;
			
			//Assuming the player specified a user
			if(playerName != null)
			{
				//Assuming the player specified a world
				if(worldName != null)
				{
					icUtils.teleportToOtherPlot(player, playerName, plotName, worldName);
				}
				else
				{
					worldName = player.getLocation().getWorld().getName();
					icUtils.teleportToOtherPlot(player, playerName, plotName, worldName);
				}
			}
			else
			{
				worldName = player.getLocation().getWorld().getName();
				icUtils.teleportToPlot(player, plotName, worldName);
			}
		}
		else
		{
			sender.sendMessage("You cannot use InfiniteClaims Commmands from the Console.");
		}
	}

}
