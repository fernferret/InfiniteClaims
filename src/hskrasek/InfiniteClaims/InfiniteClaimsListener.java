package hskrasek.InfiniteClaims;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

public class InfiniteClaimsListener implements Listener
{
	InfiniteClaims plugin;

	public InfiniteClaimsListener(InfiniteClaims instance)
	{
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent changedWorld)
	{
		Player p = changedWorld.getPlayer();
		String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "InfiniteClaims" + ChatColor.WHITE + "] ";
		World w = p.getWorld();
		ChunkGenerator cg = w.getGenerator();
		if(plugin.DEBUGGING)
		{
			plugin.log.debug("World is using InfinitPlots: " + (cg instanceof InfinitePlotsGenerator != false));
			plugin.log.debug("Player " + p.getName() + " has permissions 'infiniteclaims.auto': " + plugin.permissionManager.hasPermission(p.getName(), "infiniteclaims.auto"));
		}
		if(cg instanceof InfinitePlotsGenerator != false && plugin.permissionManager.hasPermission(p.getName(), "infiniteclaims.auto"))
		{
			int plotSize = ((InfinitePlotsGenerator)cg).getPlotSize();
			if(plugin.DEBUGGING)
			{
				plugin.log.debug("Finding Player: " + p.getName() +" a plot");
			}
			plugin.icUtils.plotAssigner(w, p, plugin.plotHeight, plotSize);
		}
		else
		{
			if(cg instanceof InfinitePlotsGenerator == true)
			{
				if(!plugin.permissionManager.hasPermission(p.getName(), "infiniteclaims.auto"))
				{
					p.sendMessage(pluginPrefix + ChatColor.DARK_AQUA + "You do not have permission to automatically claim a plot, try using /claims getplot");
				}
			}
			else
			{
				if(plugin.DEBUGGING)
				{
					plugin.log.debug("The world in question is not a InfinitePlots world");
				}
			}
		}
	}
}


