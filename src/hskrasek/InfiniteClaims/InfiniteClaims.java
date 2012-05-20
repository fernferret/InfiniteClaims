package hskrasek.InfiniteClaims;

import java.io.File;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.wepif.PermissionsResolverManager;

public class InfiniteClaims extends JavaPlugin
{
	public InfiniteClaimsLogger log;
	protected Server server;
	protected PluginManager pluginManager;
	protected InfiniteClaimsConfig config;
	public InfiniteClaimsUtilities icUtils;
	public int roadOffsetX = 4;
	public int roadOffsetZ = 4;
	public int plotHeight = 0;
	public String ownerSignPrefix;
	public String signPlacementMethod;
	public ChatColor prefixColor;
	public ChatColor ownerColor;
	public boolean DEBUGGING;
	String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "InfiniteClaims" + ChatColor.WHITE + "] ";
	public PermissionsResolverManager permissionManager;
	
	@Override
	public void onDisable()
	{
		log.info("Disabled");
		
		log = null;
		
		config = null;
	}
	
	@Override
	public void onEnable()
	{
		permissionManager = PermissionsResolverManager.getInstance();
		PermissionsResolverManager.initialize(this);
		server = this.getServer();
		pluginManager = server.getPluginManager();
		log = new InfiniteClaimsLogger("Minecraft", this);
		config = new InfiniteClaimsConfig(new File(this.getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
		icUtils = new InfiniteClaimsUtilities(this);
		pluginManager.registerEvents(new InfiniteClaimsListener(this), this);
		ownerSignPrefix = config.getString("signs.prefix");
		signPlacementMethod = config.getString("signs.placement");
		prefixColor = getColor(config.getString("signs.prefix-color"));
		ownerColor = getColor(config.getString("signs.owner-color"));
		plotHeight = config.getInt("plots.height");
		DEBUGGING = config.getBoolean("debugging");
		log.info("Enabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if(cmd.getName().equalsIgnoreCase("iclaims"))
		{
			if(args[0].equalsIgnoreCase("plot"))
			{
				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot"))
				{
					if(args.length > 1 && permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.others"))
					{
						Player otherPlayer = server.getPlayer(args[1]);
						icUtils.getPlot(otherPlayer);
						return true;
					}
					else
					{
						icUtils.getPlot(player);
						return true;
					}
				}
				else
				{
					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
					return false;
				}

			}
			else if(args[0].equalsIgnoreCase("reset"))
			{
				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.regen"))
				{
					icUtils.regeneratePlot(player);
					return true;
				}
				else
				{
					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
					return false;
				}
			}
			else if(args[0].equalsIgnoreCase("addmember"))
			{
				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.addmember"))
				{
					if(args.length > 1)
					{
						log.debug("TEST");
						icUtils.addMember(player, args[1]);
						return true;
					}
					else
					{
						player.sendMessage(pluginPrefix + "Please provide the name of a player to add to your plot.");
						return false;
					}
				}
				else
				{
					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
					return false;
				}
			}
			else if(args[0].equalsIgnoreCase("removemember"))
			{
				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.removemember"))
				{
					if(args.length > 1)
					{
						icUtils.removeMember(player, args[1]);
						return true;
					}
					else
					{
						player.sendMessage(pluginPrefix + "Please provide the naem of a player to remove from your plot.");
						return false;
					}
				}
				else
				{
					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
					return false;
				}
			}
			else
			{
				player.sendMessage(pluginPrefix + "Command not found, please try again");
			}
		}
		return false;
	}
	
	
	public WorldGuardPlugin getWorldGuard()
	{
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	log.severe("WorldEdit MUST BE INSTALLED!");
	        return null;
	    }
	    return (WorldGuardPlugin) plugin;
	}
	
	public WorldEditPlugin getWorldEdit()
	{
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		
		//WorldEdit may not be loaded
		if(plugin == null || !(plugin instanceof WorldEditPlugin)) {
			log.severe("WorlEdit MUST BE INSTALLED");
			return null;
		}
		return (WorldEditPlugin)plugin;
	}
	
	private ChatColor getColor(String colorString)
	{
		if(colorString.toLowerCase().equals("aqua"))
		{
			return ChatColor.AQUA;
		}
		if(colorString.toLowerCase().equals("black"))
		{
			return ChatColor.BLACK;
		}
		if(colorString.toLowerCase().equals("blue"))
		{
			return ChatColor.BLUE;
		}
		if(colorString.toLowerCase().equals("darkaqua"))
		{
			return ChatColor.DARK_AQUA;
		}
		if(colorString.toLowerCase().equals("darkblue"))
		{
			return ChatColor.DARK_BLUE;
		}
		if(colorString.toLowerCase().equals("darkgray"))
		{
			return ChatColor.DARK_GRAY;
		}
		if(colorString.toLowerCase().equals("darkgreen"))
		{
			return ChatColor.DARK_GREEN;
		}
		if(colorString.toLowerCase().equals("darkpurple"))
		{
			return ChatColor.DARK_PURPLE;
		}
		if(colorString.toLowerCase().equals("darkred"))
		{
			return ChatColor.DARK_RED;
		}
		if(colorString.toLowerCase().equals("gold"))
		{
			return ChatColor.GOLD;
		}
		if(colorString.toLowerCase().equals("gray"))
		{
			return ChatColor.GRAY;
		}
		if(colorString.toLowerCase().equals("green"))
		{
			return ChatColor.GREEN;
		}
		if(colorString.toLowerCase().equals("purple"))
		{
			return ChatColor.LIGHT_PURPLE;
		}
		if(colorString.toLowerCase().equals("red"))
		{
			return ChatColor.RED;
		}
		if(colorString.toLowerCase().equals("white"))
		{
			return ChatColor.WHITE;
		}
		if(colorString.toLowerCase().equals("yellow"))
		{
			return ChatColor.YELLOW;
		}
		return ChatColor.BLACK;
	}
}