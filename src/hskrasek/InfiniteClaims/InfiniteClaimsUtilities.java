package hskrasek.InfiniteClaims;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
		plotFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "plots.yml");
	}
	
	public void plotAssigner(World w, Player p, int y, int plotSize)
	{
		com.sk89q.worldguard.LocalPlayer lp = wgp.wrapPlayer(p);
		startLoc = new Location(w, plugin.roadOffsetX, y, plugin.roadOffsetZ);
		RegionManager rm = wgp.getRegionManager(w);
		int playerRegionCount = rm.getRegionCountOfPlayer(lp);
		Location workingLocation = startLoc; // workingLocation will be used for searching for an empty plot
		
		if(playerRegionCount == 0)
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
				String plotName = "Plot" + p.getName() + failedAttemptCount; // failedAttemptCount is appended at the end for uniqueness
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
				p.teleport(new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize)));
				savePlot(p, new Location(w, bottomRight.getX() + (plotSize / 2), y + 1, bottomRight.getZ() + (plotSize)));
				p.sendMessage(pluginPrefix + "Teleporting you to your plots' entrance.");
				p.sendMessage(pluginPrefix + "You can return to your plot with " + ChatColor.YELLOW + "/iclaims plot");
			}
			else
			{
				p.sendMessage(pluginPrefix + "Unable to find an unclaimed location.  Please exit the world and try again.  If this continues, please notify an admin.");
			}			
		}
	}
	
	public void savePlot(Player thePlayer, Location theLocation)
	{
		YamlConfiguration plots = new YamlConfiguration();
		double x = theLocation.getX();
		double z = theLocation.getZ();
		World theWorld = theLocation.getWorld();
		plots.set("plots." + thePlayer.getName() + ".world", theWorld.getName());
		plots.set("plots." + thePlayer.getName() + ".x", x);
		plots.set("plots." + thePlayer.getName() + ".z", z);
		try {
			plots.save(plotFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void regeneratePlot(Player thePlayer)
	{
//		 	Region region = session.getSelection(player.getWorld());
//	        Mask mask = session.getMask();
//	        session.setMask(null);
//	        player.getWorld().regenerate(region, editSession);
//	        session.setMask(mask);
//	        player.print("Region regenerated.");
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
			int plotSize = ((InfinitePlotsGenerator)cg).getPlotSize();
			LocalSession theSession = wep.getSession(thePlayer);
			EditSession editSession = wep.createEditSession(thePlayer);
			Mask mask = theSession.getMask();
			theSession.setMask(null);
			tempPlayer.getWorld().regenerate(regionToRegenerate, editSession);
			theSession.setMask(mask);
			thePlayer.sendMessage(pluginPrefix + " Your plot has been reset.");
		}
		else
		{
			thePlayer.sendMessage("Please return to your plot to reset it");
		}
		
	}
	
	public void addMember(Player plotOwner, String playerToAdd)
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
			members.addPlayer(playerToAdd);
			
			ownersPlot.setMembers(members);
			
			try 
			{
				mgr.save();
			} catch (ProtectionDatabaseException e) 
			{
				e.printStackTrace();
			}
			
			plotOwner.sendMessage(pluginPrefix + "Added '" + ChatColor.YELLOW + playerToAdd + ChatColor.WHITE + "' to your plot.");
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void getPlot(Player thePlayer)
	{
		YamlConfiguration plots = new YamlConfiguration();
		try {
			plots.load(plotFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		plugin.log.debug((String) plots.get("plots." + thePlayer.getName() + ".world") + ", " + (Double)plots.get("plots."+thePlayer.getName() + ".x") + ", " + (Double)plots.get("plots."+thePlayer.getName() + ".z"));
		World plotWorld = new WorldCreator((String) plots.get("plots." + thePlayer.getName() + ".world")).createWorld();
		double x = (Double)plots.get("plots."+thePlayer.getName() + ".x");
		double z = (Double)plots.get("plots." + thePlayer.getName() + ".z");
		thePlayer.teleport(new Location(plotWorld, x, plugin.plotHeight + 1, z, 180, 0));
	}
	
	public static void placeSign(String plotOwnerPrefix, String plotOwner, Block theBlock, BlockFace facingDirection)
	{
		theBlock.setType(Material.SIGN_POST);
		Sign theSign = (Sign)theBlock.getState();
		theSign.setLine(1, plugin.prefixColor + plotOwnerPrefix);
		theSign.setLine(2, plugin.ownerColor + plotOwner);
		
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