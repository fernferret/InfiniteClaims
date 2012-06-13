package hskrasek.InfiniteClaims;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
//import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class InfiniteClaimsUtilities 
{
	static InfiniteClaims plugin;
	static Location startLoc;
	static WorldGuardPlugin wgp;
	WorldEditPlugin wep;
	static String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "InfiniteClaims" + ChatColor.WHITE + "] ";
	static int walkwaySize = 7;
	File plotFile;
	
	public InfiniteClaimsUtilities(InfiniteClaims instance)
	{
		plugin = instance;
		wgp = plugin.getWorldGuard();
		wep = plugin.getWorldEdit();
		setupDataFolders();
	}
	
	private void setupDataFolders()
	{
		if(plugin.DEBUGGING)
		{
			plugin.log.debug("Now setting up plot folders...");
		}
		Server server = plugin.getServer();
		List<World> worlds = server.getWorlds();
		for(World world : worlds)
		{
			ChunkGenerator cg = world.getGenerator();
			if(cg instanceof InfinitePlotsGenerator)
			{
				if(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName()).exists())
				{
					plotFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName() + File.separator + "plots.yml");
					if(!plotFile.exists())
					{
						if(plugin.DEBUGGING)
						{
							plugin.log.debug("Plot file @ " + plotFile.getAbsolutePath() + " doesnt exist");
						}
						try {
							plotFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else
					{
						if(plugin.DEBUGGING)
						{
							plugin.log.debug("Plot file already exist!");
						}
					}
				}
				else
				{
					new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName()).mkdir();
					plotFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName() + File.separator + "plots.yml");
					try {
						plotFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void plotAssigner(World w, Player p, int y, int plotSize)
	{
		com.sk89q.worldguard.LocalPlayer lp = wgp.wrapPlayer(p);
		startLoc = new Location(w, plugin.roadOffsetX, y, plugin.roadOffsetZ);
		RegionManager rm = wgp.getRegionManager(w);
		int playerRegionCount = rm.getRegionCountOfPlayer(lp);
		Location workingLocation = startLoc; // workingLocation will be used for searching for an empty plot
		
		if(playerRegionCount < 1)
		{
			int regionSpacing = plotSize + walkwaySize;
			int failedAttemptCount = 0;
			boolean owned = true;
			
			Map<String, ProtectedRegion> regions = rm.getRegions();
			Set<String> keySet = regions.keySet();
			Object[] keys = keySet.toArray();
			int failedAttemptMaxCount = keys.length + 1; // finding an owned region counts as a failed attempt, so it's possible to validly have that many failures

			p.sendMessage(pluginPrefix + "Hi " + p.getName() + ".  You don't seem to have a plot. Let me fix that for you!");
			p.sendMessage(pluginPrefix + "Size for plots in this world: " + plotSize);
			
			while(owned && failedAttemptCount < failedAttemptMaxCount)
			{
				// this block will execute until the owned flag is set to false or until failedAttemptCount reaches the max
				
				owned = false; // ensures the loop will only execute once if no plots are owned.
				Random rnd = new Random();
				int plotDir = rnd.nextInt(8);
				List<Location> checkedLocations = new ArrayList<Location>();
				
				if(plotDir == 0)
				{
					// one plot to the right of current workingLocation
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ());
				}
				else if(plotDir == 1)
				{
					// one plot to the right and up of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 2)
				{
					// one plot up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 3)
				{
					// one plot to the left and up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() + regionSpacing);					
				}
				else if(plotDir == 4)
				{
					// one plot to the left of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ());					
				}
				else if(plotDir == 5)
				{
					// one plot to the left and down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 6)
				{
					// one plot down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 7)
				{
					// one plot to the right and down of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}

				if(!checkedLocations.contains(workingLocation))
				{
					// only check the region if it hasn't already been checked, otherwise it will falsely update the failedAttemptCount
					checkedLocations.add(workingLocation);

					for (Object key : keys)
					{
						ProtectedRegion pr = regions.get(key);	
						owned = pr.contains((int)workingLocation.getX(), (int)workingLocation.getY(), (int)workingLocation.getZ());

						if(owned)
						{
							// if the ProtectedRegion contains the coord's of the workingLocation, then 
							// it's owned and we need to reset workingLocation to a new spot
							failedAttemptCount++;
							break;
						}							
					}							
				}					
			}
			
			if(failedAttemptCount < failedAttemptMaxCount)
			{
				Location bottomRight = workingLocation; // not really needed, I did it just for clarity
                Location bottomLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ());
                Location topRight = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + (plotSize - 1));
                Location topLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ() + (plotSize - 1));
				CuboidSelection plot = new CuboidSelection(w, bottomRight, topLeft);
				Region tempRegion = null;
				try {
					tempRegion = plot.getRegionSelector().getRegion();
					tempRegion.expand(new Vector(0, w.getMaxHeight(), 0), new Vector(0, (-(plugin.plotHeight)+1), 0));
				} 
				catch (IncompleteRegionException e) 
				{
					p.sendMessage(e.getMessage());
				}
				catch(RegionOperationException e)
				{
					p.sendMessage(e.getMessage());
				}
				String plotName = p.getName() + "Plot" + (playerRegionCount + 1); // failedAttemptCount is appended at the end for uniqueness
				p.sendMessage(pluginPrefix + "I've found a plot for you! Naming it: " + ChatColor.YELLOW + plotName);
				
				BlockVector minPoint = tempRegion.getMinimumPoint().toBlockVector();
				BlockVector maxPoint = tempRegion.getMaximumPoint().toBlockVector();
				ProtectedRegion playersPlot = new ProtectedCuboidRegion(plotName, minPoint, maxPoint);
				DefaultDomain owner = new DefaultDomain();
				owner.addPlayer(lp);
				playersPlot.setOwners(owner);
				
				
				RegionManager mgr = wgp.getGlobalRegionManager().get(w) ;
				mgr.addRegion(playersPlot);
				try 
				{
					mgr.save();
				} catch (ProtectionDatabaseException e) 
				{
					e.printStackTrace();
				}
				
				if(plugin.signPlacementMethod.equals("entrance"))
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Placing signs on the entrance of "+ p.getName() +"'s plot");
					}
					Location entranceLocation1 = new Location(w, bottomRight.getX() + (plotSize / 2) - 2, y + 3, bottomRight.getZ() + (plotSize));
	                Block entranceBlock1 = entranceLocation1.getBlock();
	                Location entranceLocation2 = new Location(w, bottomRight.getX() + (plotSize / 2) + 2, y + 3, bottomRight.getZ() + (plotSize));
	                Block entranceBlock2 = entranceLocation2.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), entranceBlock1, BlockFace.SOUTH);
	                placeSign(plugin.ownerSignPrefix, p.getName(), entranceBlock2, BlockFace.SOUTH);
				}
				else if(plugin.signPlacementMethod.equals("corners"))
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Placing signs on the corners of "+ p.getName() +"'s plot");
					}
					// creates a sign for the bottom right corner
					Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
	                Block brBlock = bottomRightTest.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), brBlock, BlockFace.NORTH_WEST);
	                
					// creates a sign for the bottom left corner
	                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
	                Block blBlock = bottomLeftTest.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), blBlock, BlockFace.NORTH_EAST);

					// creates a sign for the top right corner
	                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
	                Block trBlock = topRightSign.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), trBlock, BlockFace.SOUTH_WEST);
	                
	                // creates a sign for the top left corner
	                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
	                Block tlBlock = topLeftSign.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), tlBlock, BlockFace.SOUTH_EAST);                
				}
                
                // teleports player to their plot
				p.teleport(new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize), 180, 0));
				savePlot(p,"plot"+(playerRegionCount + 1), new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize)));
				p.sendMessage(pluginPrefix + "Teleporting you to your plots' entrance.");
				p.sendMessage(pluginPrefix + "You can return to your plot with " + ChatColor.YELLOW + "/iclaims plot");
			}
			else
			{
				p.sendMessage(pluginPrefix + "Unable to find an unclaimed location.  Please exit the world and try again.  If this continues, please notify an admin.");
			}			
		}
	}
	
	public void retrievePlot(World w, Player p, int y, int plotSize)
	{
		com.sk89q.worldguard.LocalPlayer lp = wgp.wrapPlayer(p);
		startLoc = new Location(w, plugin.roadOffsetX, y, plugin.roadOffsetZ);
		RegionManager rm = wgp.getRegionManager(w);
		int playerRegionCount = rm.getRegionCountOfPlayer(lp);
		Location workingLocation = startLoc;
		
		if(playerRegionCount < plugin.maxPlots)
		{
			int regionSpacing = plotSize + walkwaySize;
			int failedAttemptCount = 0;
			boolean owned = true;
			
			Map<String, ProtectedRegion> regions = rm.getRegions();
			Set<String> keySet = regions.keySet();
			Object[] keys = keySet.toArray();
			int failedAttemptMaxCount = keys.length + 1; // finding an owned region counts as a failed attempt, so it's possible to validly have that many failures

			p.sendMessage(pluginPrefix + "Hi " + p.getName() + ".  You don't seem to have a plot. Let me fix that for you!");
			p.sendMessage(pluginPrefix + "Size for plots in this world: " + plotSize);
			
			while(owned && failedAttemptCount < failedAttemptMaxCount)
			{
				// this block will execute until the owned flag is set to false or until failedAttemptCount reaches the max
				
				owned = false; // ensures the loop will only execute once if no plots are owned.
				Random rnd = new Random();
				int plotDir = rnd.nextInt(8);
				List<Location> checkedLocations = new ArrayList<Location>();
				
				if(plotDir == 0)
				{
					// one plot to the right of current workingLocation
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ());
				}
				else if(plotDir == 1)
				{
					// one plot to the right and up of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 2)
				{
					// one plot up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + regionSpacing);		
				}
				else if(plotDir == 3)
				{
					// one plot to the left and up of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() + regionSpacing);					
				}
				else if(plotDir == 4)
				{
					// one plot to the left of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ());					
				}
				else if(plotDir == 5)
				{
					// one plot to the left and down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 6)
				{
					// one plot down of current workingLocation													
					workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() - regionSpacing);					
				}
				else if(plotDir == 7)
				{
					// one plot to the right and down of current workingLocation							
					workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() - regionSpacing);					
				}

				if(!checkedLocations.contains(workingLocation))
				{
					// only check the region if it hasn't already been checked, otherwise it will falsely update the failedAttemptCount
					checkedLocations.add(workingLocation);

					for (Object key : keys)
					{
						ProtectedRegion pr = regions.get(key);	
						owned = pr.contains((int)workingLocation.getX(), (int)workingLocation.getY(), (int)workingLocation.getZ());

						if(owned)
						{
							// if the ProtectedRegion contains the coord's of the workingLocation, then 
							// it's owned and we need to reset workingLocation to a new spot
							failedAttemptCount++;
							break;
						}							
					}							
				}					
			}
			
			if(failedAttemptCount < failedAttemptMaxCount)
			{
				Location bottomRight = workingLocation; // not really needed, I did it just for clarity
                Location bottomLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ());
                Location topRight = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + (plotSize - 1));
                Location topLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ() + (plotSize - 1));
				CuboidSelection plot = new CuboidSelection(w, bottomRight, topLeft);
				Region tempRegion = null;
				try {
					tempRegion = plot.getRegionSelector().getRegion();
					tempRegion.expand(new Vector(0, w.getMaxHeight(), 0), new Vector(0, (-(plugin.plotHeight)+1), 0));
				} 
				catch (IncompleteRegionException e) 
				{
					p.sendMessage(e.getMessage());
				}
				catch(RegionOperationException e)
				{
					p.sendMessage(e.getMessage());
				}
				String plotName = p.getName() + "Plot" + (playerRegionCount + 1); // failedAttemptCount is appended at the end for uniqueness
				p.sendMessage(pluginPrefix + "I've found a plot for you! Naming it: " + plotName);
				
				BlockVector minPoint = tempRegion.getMinimumPoint().toBlockVector();
				BlockVector maxPoint = tempRegion.getMaximumPoint().toBlockVector();
				ProtectedRegion playersPlot = new ProtectedCuboidRegion(plotName, minPoint, maxPoint);
				DefaultDomain owner = new DefaultDomain();
				owner.addPlayer(lp);
				playersPlot.setOwners(owner);
				
				
				RegionManager mgr = wgp.getGlobalRegionManager().get(w) ;
				mgr.addRegion(playersPlot);
				try 
				{
					mgr.save();
				} catch (ProtectionDatabaseException e) 
				{
					e.printStackTrace();
				}
				
				if(plugin.signPlacementMethod.equals("entrance"))
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Placing signs on the entrance of "+ p.getName() +"'s plot");
					}
					Location entranceLocation1 = new Location(w, bottomRight.getX() + (plotSize / 2) - 2, y + 3, bottomRight.getZ() + (plotSize));
	                Block entranceBlock1 = entranceLocation1.getBlock();
	                Location entranceLocation2 = new Location(w, bottomRight.getX() + (plotSize / 2) + 2, y + 3, bottomRight.getZ() + (plotSize));
	                Block entranceBlock2 = entranceLocation2.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), entranceBlock1, BlockFace.SOUTH);
	                placeSign(plugin.ownerSignPrefix, p.getName(), entranceBlock2, BlockFace.SOUTH);
				}
				else if(plugin.signPlacementMethod.equals("corners"))
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Placing signs on the corners of "+ p.getName() +"'s plot");
					}
					// creates a sign for the bottom right corner
					Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
	                Block brBlock = bottomRightTest.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), brBlock, BlockFace.NORTH_WEST);
	                
					// creates a sign for the bottom left corner
	                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
	                Block blBlock = bottomLeftTest.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), blBlock, BlockFace.NORTH_EAST);

					// creates a sign for the top right corner
	                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
	                Block trBlock = topRightSign.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), trBlock, BlockFace.SOUTH_WEST);
	                
	                // creates a sign for the top left corner
	                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
	                Block tlBlock = topLeftSign.getBlock();
	                placeSign(plugin.ownerSignPrefix, p.getName(), tlBlock, BlockFace.SOUTH_EAST);                
				}
                
                // teleports player to their plot
				p.teleport(new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize), 180, 0));
				savePlot(p,"plot"+(playerRegionCount + 1), new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize)));
				p.sendMessage(pluginPrefix + "Teleporting you to your plots' entrance.");
				p.sendMessage(pluginPrefix + "You can return to your plot with " + ChatColor.YELLOW + "/iclaims plot");
			}
			else
			{
				p.sendMessage(pluginPrefix + "Unable to find an unclaimed location.  Please exit the world and try again.  If this continues, please notify an admin.");
			}			
		}
	}
	
	public void savePlot(Player thePlayer,String plotName, Location theLocation)
	{
		YamlConfiguration plots = new YamlConfiguration();
		try {
			plots.load(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + theLocation.getWorld().getName() + File.separator + "plots.yml"));
		} catch (FileNotFoundException e1) {
			plugin.log.severe("Could not find the 'plots.yml' file for the world: " + theLocation.getWorld().getName());
			e1.printStackTrace();
		} catch (IOException e1) {
			plugin.log.severe("There was an error reading the 'plots.yml' file for " + theLocation.getWorld().getName() +". Please submit a ticket to the plugin developer.");
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			plugin.log.severe("The format for 'plots.yml' file for the world " + theLocation.getWorld().getName() + " is incorrect, please submit a ticket to the plugin developer.");
			e1.printStackTrace();
		}
		double x = theLocation.getX();
		double z = theLocation.getZ();
		plots.set("plots." + thePlayer.getName() + "." + plotName + ".x", x);
		plots.set("plots." + thePlayer.getName() + "." + plotName + ".z", z);
		try {
			plots.save(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + theLocation.getWorld().getName() + File.separator + "plots.yml"));
		} catch (IOException e) {
			plugin.log.severe("There was an error saving the plot for " + thePlayer.getName() + " for the world " + theLocation.getWorld().getName() + " please submit a ticket with the plugin developer");
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO Change reset plot to accept a specific plot name, which will then select that specifc WorldGuard region, and regenerate it.
	 * TODO FIX RESET!!!
	 * @param thePlayer
	 */
	public void regeneratePlot(Player thePlayer)
	{
		World claimsWorld = thePlayer.getWorld();
		ChunkGenerator cg = claimsWorld.getGenerator();
		com.sk89q.worldguard.LocalPlayer localPlayer = wgp.wrapPlayer(thePlayer);
		if(cg instanceof InfinitePlotsGenerator == true)
		{
			RegionManager mgr = wgp.getGlobalRegionManager().get(claimsWorld);
			Map<String, ProtectedRegion> regions = mgr.getRegions();
			Set<String> regionsIds = regions.keySet();
			String regionToRegenId = "";
			ProtectedRegion tempRegion = null;
			Region regionToRegenerate = null;
			for(String regionId : regionsIds)
			{
				ProtectedRegion playersPlot = regions.get(regionId);
				if(!playersPlot.isOwner(localPlayer))
				{
					continue;
				}
				else
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Found region for player: " + thePlayer.getName() + " Region - " + regionId);
					}
					regionToRegenId = regionId;
					break;
				}
			}
			
			tempRegion = mgr.getRegion(regionToRegenId);
			
			regionToRegenerate = selectPlot(thePlayer, localPlayer, tempRegion);
			if(plugin.DEBUGGING)
			{
				plugin.log.debug("Region to Regenerate: " + regionToRegenerate + " Area of the Region: " + regionToRegenerate.getArea());
			}
			BukkitPlayer tempPlayer = new BukkitPlayer(wep, wep.getServerInterface(), thePlayer);
			LocalSession theSession = wep.getSession(thePlayer);
			EditSession editSession = wep.createEditSession(thePlayer);
			Mask mask = theSession.getMask();
			theSession.setMask(null);
			try {
				List<Vector> changes = new ArrayList<Vector>(4);
				changes.add(new Vector(1,0,0));
				changes.add(new Vector(-1,0,0));
				changes.add(new Vector(0,0,-1));
				changes.add(new Vector(0,0,1));
				
				regionToRegenerate.contract(changes.toArray(new Vector[0]));
				plugin.log.debug("Region with contract: " + regionToRegenerate);
			} catch (RegionOperationException e) {
				
				e.printStackTrace();
			}
			tempPlayer.getWorld().regenerate(regionToRegenerate, editSession);
			theSession.setMask(mask);
			
//			Set<Vector2D> chunks = regionToRegenerate.getChunks();
//			Iterator<Vector2D> chunkCords = chunks.iterator();
//			
//			while(chunkCords.hasNext())
//			{
//				Vector2D chunkCoord = chunkCords.next();
//				if(plugin.DEBUGGING)
//				{
//					plugin.log.debug(String.format("Now refreshing chunk (%d,%d)", chunkCoord.getBlockX(), chunkCoord.getBlockZ()));
//				}
//				claimsWorld.refreshChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ());
//			}
			
			ProtectedRegion test = new ProtectedCuboidRegion(thePlayer.getName()+"plotTest",regionToRegenerate.getMinimumPoint().toBlockVector(), regionToRegenerate.getMaximumPoint().toBlockVector());
			mgr.removeRegion(regionToRegenId);
			mgr.addRegion(test);
			
			try {
				mgr.save();
			} catch (ProtectionDatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			thePlayer.sendMessage(pluginPrefix + " Your plot has been reset.");
		}
		else
		{
			thePlayer.sendMessage("Please return to your plot to reset it");
		}
		
	}
	
	public void addMember(Player plotOwner, String playerToAdd, String plotName, World plotWorld)
	{
		ChunkGenerator cg = plotWorld.getGenerator();
//		com.sk89q.worldguard.LocalPlayer localPlayer = wgp.wrapPlayer(plotOwner);
		if(cg instanceof InfinitePlotsGenerator == true)
		{
			RegionManager mgr = wgp.getGlobalRegionManager().get(plotWorld);
			
//			Map<String, ProtectedRegion> regions = mgr.getRegions();
//			Set<String> regionsIds = regions.keySet();
			String regionId = plotOwner.getName() + plotName;
			plugin.log.debug("RegionID: " + regionId);
//			for(String region : regionsIds)
//			{
//				ProtectedRegion playersPlot = regions.get(region);
//				if(!playersPlot.isOwner(localPlayer))
//				{
//					continue;
//				}
//				else
//				{
//					regionId = region;
//					if(plugin.DEBUGGING)
//					{
//						plugin.log.debug("Found region for player: " + plotOwner.getName() + " Region - " + regionId);
//					}
//					break;
//				}
//			}
			
			ProtectedRegion ownersPlot = mgr.getRegion(regionId);
			plugin.log.debug("ProtectedRegion: " + ownersPlot.getId());
			DefaultDomain members = ownersPlot.getMembers();
			members.addPlayer(playerToAdd);
			
			ownersPlot.setMembers(members);
			
			try 
			{
				mgr.save();
			} catch (ProtectionDatabaseException e) 
			{
				e.printStackTrace();
			}
			
			plotOwner.sendMessage(pluginPrefix + "Added '" + ChatColor.YELLOW + playerToAdd + ChatColor.WHITE + "' to plot: " + ChatColor.YELLOW + plotName);
			
			return;
		}
		else
		{
			plotOwner.sendMessage(pluginPrefix + "Please return to your plot to add members");
		}
	}
	
	public void removeMember(Player plotOwner, String playerToRemove)
	{
		World claimsWorld = plotOwner.getWorld();
		ChunkGenerator cg = claimsWorld.getGenerator();
		com.sk89q.worldguard.LocalPlayer localPlayer = wgp.wrapPlayer(plotOwner);
		if(cg instanceof InfinitePlotsGenerator == true)
		{
			RegionManager mgr = wgp.getGlobalRegionManager().get(claimsWorld);
			Map<String, ProtectedRegion> regions = mgr.getRegions();
			Set<String> regionsIds = regions.keySet();
			String regionId = "";
			for(String region : regionsIds)
			{
				ProtectedRegion playersPlot = regions.get(region);
				if(!playersPlot.isOwner(localPlayer))
				{
					continue;
				}
				else
				{
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Found region for player: " + plotOwner.getName() + " Region - " + regionId);
					}
					regionId = region;
					break;
				}
			}
			ProtectedRegion ownersPlot = regions.get(regionId);
			DefaultDomain members = ownersPlot.getMembers();
			members.removePlayer(playerToRemove);
			
			ownersPlot.setMembers(members);
			
			try 
			{
				mgr.save();
			} catch (ProtectionDatabaseException e) 
			{
				e.printStackTrace();
			}
			
			plotOwner.sendMessage(pluginPrefix + "Removed '" + ChatColor.YELLOW + playerToRemove + ChatColor.WHITE + "' from your plot.");
			
			return;
		}
	}
	
	private Region selectPlot(Player player, com.sk89q.worldguard.LocalPlayer localPlayer, ProtectedRegion region)
	{
		ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion)region;
		Vector pt1 = cuboid.getMinimumPoint();
		Vector pt2 = cuboid.getMaximumPoint();
		CuboidSelection selection = new CuboidSelection(player.getWorld(), pt1, pt2);
		try {
			return selection.getRegionSelector().getRegion();
		} catch (IncompleteRegionException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void listPlots(Player thePlayer, String worldName)
	{
		YamlConfiguration plots = new YamlConfiguration();
		try {
			plots.load(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + worldName + File.separator + "plots.yml"));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			
			e.printStackTrace();
		}
		Set<String> keys = plots.getKeys(true);
		Iterator<String> keyIt = keys.iterator();
		thePlayer.sendMessage(pluginPrefix + "Your plots:");
		while(keyIt.hasNext())
		{
			String currentKey = keyIt.next();
			if(currentKey.contains(thePlayer.getName()))
			{
				if(!currentKey.contains("x") && !currentKey.contains("z") && !currentKey.equalsIgnoreCase("plots." + thePlayer.getName()))
				{
					String[] splitArray = currentKey.split("\\.");
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Current Key: " + currentKey);
						for(String item : splitArray)
						{
							plugin.log.debug("Key Split: " +item);
						}
					}
					thePlayer.sendMessage(pluginPrefix + ChatColor.YELLOW + splitArray[2]);
				}
			}
		}
	}
	
	public void teleportToPlot(Player thePlayer, String plotName, String worldName)
	{
		YamlConfiguration plots = new YamlConfiguration();
		
		try {
			plots.load(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + worldName + File.separator + "plots.yml"));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			
			e.printStackTrace();
		}
		
		if(plugin.DEBUGGING)
		{
			plugin.log.debug("Player:" +  thePlayer.getName());
			plugin.log.debug("Plotname:" + plotName);
			plugin.log.debug("World:" + worldName);
			plugin.log.debug("X:" + plots.getString("plots." + thePlayer.getName() + "." + plotName + ".x"));
			plugin.log.debug("Z:" + plots.getString("plots." + thePlayer.getName() + "." + plotName + ".z"));
		}
		World plotWorld = new WorldCreator(worldName).createWorld();
		double x = (Double)plots.get("plots."+thePlayer.getName() + "." + plotName + ".x");
		double z = (Double)plots.get("plots." + thePlayer.getName() + "." + plotName + ".z");
		thePlayer.teleport(new Location(plotWorld, x, plugin.plotHeight + 2, z, 180, 0));
	}
	
	public void teleportToOtherPlot(Player thePlayer, String otherPlayer, String plotName, String worldName)
	{
		YamlConfiguration plots = new YamlConfiguration();
		try {
			plots.load(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + worldName + File.separator + "plots.yml"));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			
			e.printStackTrace();
		}
		if(plugin.DEBUGGING)
		{
			plugin.log.debug("Player:" +  otherPlayer);
			plugin.log.debug("Plotname:" + plotName);
			plugin.log.debug("World:" + worldName);
			plugin.log.debug("X:" + plots.getString("plots." + otherPlayer + "." + plotName + ".x"));
			plugin.log.debug("Z:" + plots.getString("plots." + otherPlayer + "." + plotName + ".z"));
		}
		World plotWorld = new WorldCreator(worldName).createWorld();
		double x = (Double)plots.get("plots."+otherPlayer + "." + plotName + ".x");
		double z = (Double)plots.get("plots." + otherPlayer + "." + plotName + ".z");
		thePlayer.teleport(new Location(plotWorld, x, plugin.plotHeight + 2, z, 180, 0));
	}
	
	public void legacyUpdate(Server theServer)
	{
		plugin.log.info("Now looking for all InfinitePlots worlds...");
		List<World> worlds = theServer.getWorlds();
		for(World world : worlds)
		{
			ChunkGenerator cg = world.getGenerator();
			if(cg instanceof InfinitePlotsGenerator)
			{
				plugin.log.info("Found a InfinitePlots world! Now converting plots for world: " + world.getName());
				RegionManager mgr = wgp.getRegionManager(world);
				if(plugin.DEBUGGING)
				{
					plugin.log.debug("There are '" + mgr.getRegions().size() + "' plots for world: " + world.getName());
				}
				
				Map<String, ProtectedRegion> regions = mgr.getRegions();
				Iterator<String> regionIterator = regions.keySet().iterator();
				
				for(String currentPlotId : regions.keySet())
				{
					ProtectedRegion currentPlot = mgr.getRegion(currentPlotId);
					String plotOwner = currentPlot.getOwners().toPlayersString();
					BlockVector plotMinimum = currentPlot.getMinimumPoint();
					BlockVector plotMaximum = currentPlot.getMaximumPoint();
					ProtectedCuboidRegion convertedPlot = new ProtectedCuboidRegion(plotOwner + "plot" + 1, plotMinimum, plotMaximum);
					
					mgr.removeRegion(currentPlotId);
					mgr.addRegion(convertedPlot);
					
					if(plugin.DEBUGGING)
					{
						plugin.log.debug("Plot name before: " + currentPlotId + "\nPlot name after: " + convertedPlot.getId());
						plugin.log.debug(String.format("%@'s Min Point: %d Max Point: %d", convertedPlot.getId(), convertedPlot.getMinimumPoint(), convertedPlot.getMaximumPoint()));
//							plugin.log.debug("Does Manager contain old region?")
					}
				}
				
				
				try {
					mgr.save();
					plugin.log.info("Saving regions...");
				} catch (ProtectionDatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void placeSign(String plotOwnerPrefix, String plotOwner, Block theBlock, BlockFace facingDirection)
	{
		theBlock.setType(Material.SIGN_POST);
		Sign theSign = (Sign)theBlock.getState();
		theSign.setLine(1, plugin.prefixColor + plotOwnerPrefix);
		if(plotOwner.length() > 15)
		{
			String plotOwnerFirst = plotOwner.substring(0, 13);
			String plotOwnerSecond = plotOwner.substring(plotOwner.length() - 1);
			theSign.setLine(2, plugin.ownerColor + plotOwnerFirst);
			theSign.setLine(3, plugin.ownerColor + plotOwnerSecond);
		}
		else
		{
			theSign.setLine(2, plugin.ownerColor + plotOwner);
		}
		
		byte ne = 0xA; // North East
		byte se = 0xE; // South East
		byte sw = 0x2; // South West
		byte nw = 0x6; // North West
		byte w = 0x4;
		byte s = 0x0;
		
		if(facingDirection == BlockFace.SOUTH_WEST)
		{
			theSign.setRawData(sw);
		}
		if(facingDirection == BlockFace.SOUTH_EAST)
		{
			theSign.setRawData(se);
		}
		if(facingDirection == BlockFace.NORTH_EAST)
		{
			theSign.setRawData(ne);
		}
		if(facingDirection == BlockFace.NORTH_WEST)
		{
			theSign.setRawData(nw);
		}
		if(facingDirection == BlockFace.SOUTH)
		{
			theSign.setRawData(s);
		}
		theSign.update();
	}
}