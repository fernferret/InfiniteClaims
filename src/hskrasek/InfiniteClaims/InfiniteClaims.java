package hskrasek.InfiniteClaims;

import hskrasek.InfiniteClaims.commands.HelpCommand;
import hskrasek.InfiniteClaims.commands.PlotTeleportCommand;

import java.io.File;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

import com.pneumaticraft.commandhandler.CommandHandler;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.wepif.PermissionsResolverManager;

public class InfiniteClaims extends JavaPlugin
{
	public InfiniteClaimsLogger log;
	private CommandHandler commandHandler;
	private InfiniteClaimsPerms permissionsInterface;
	protected Server server;
	protected PluginManager pluginManager;
	protected InfiniteClaimsConfig config;
	public InfiniteClaimsUtilities icUtils;
	public int roadOffsetX = 4;
	public int roadOffsetZ = 4;
	public int plotHeight = 0;
	public int maxPlots = 1;
	public String ownerSignPrefix;
	public String signPlacementMethod;
	public ChatColor prefixColor;
	public ChatColor ownerColor;
	public boolean DEBUGGING;
	private String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "InfiniteClaims" + ChatColor.WHITE + "] ";
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
		icUtils = new InfiniteClaimsUtilities(this);
		config = new InfiniteClaimsConfig(new File(this.getDataFolder().getAbsolutePath() + File.separator + "config.yml"), this);
		permissionsInterface = new InfiniteClaimsPerms(this);
		commandHandler = new CommandHandler(this,permissionsInterface);
		pluginManager.registerEvents(new InfiniteClaimsListener(this), this);
		pluginManager.registerEvents(new Listener() 
		{
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event)
			{
				if(event.getPlayer().isOp())
				{
					log.debug(event.getPlayer().getName() + " is an OP!");
				}
				else
				{
					log.debug("NOPE");
				}
			}
		}, this);
		ownerSignPrefix = config.getString("signs.prefix");
		signPlacementMethod = config.getString("signs.placement");
		prefixColor = getColor(config.getString("signs.prefix-color"));
		ownerColor = getColor(config.getString("signs.owner-color"));
		plotHeight = config.getInt("plots.height");
		maxPlots = config.getInt("plots.max-plots");
		DEBUGGING = config.getBoolean("debugging");
		this.registerCommands();
		log.info("Enabled");
	}
	
	public InfiniteClaimsLogger getLog() {
		return log;
	}
	
	public InfiniteClaimsUtilities getIcUtils() {
		return icUtils;
	}


//	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
//	{
//		Player player = null;
//		if (sender instanceof Player) {
//			player = (Player) sender;
//		}
//		if(cmd.getName().equalsIgnoreCase("iclaims"))
//		{
//			if(args.length == 0 || args[0].equalsIgnoreCase("help"))
//			{
//				String message = pluginPrefix + "Available commands:\n" +
//				"     " + ChatColor.YELLOW + "/iclaims plot" + ChatColor.WHITE + "- Takes you to your plot\n"
//				+ "     " + ChatColor.YELLOW + "/iclaims reset" + ChatColor.WHITE + "- Resets your plot\n"
//				+ "     " + ChatColor.YELLOW + "/iclaims addmember" + ChatColor.WHITE + "- Adds a member to your plot\n"
//				+ "     " + ChatColor.YELLOW + "/iclaims removemember" + ChatColor.WHITE + "- Removes a member from your plot\n";
//				player.sendMessage(message);
//				return true;
//			}
//			if(args[0].equalsIgnoreCase("plot"))
//			{
//				if(args.length <= 1)
//				{
//					player.sendMessage(pluginPrefix + "Correct Usage: " + ChatColor.YELLOW + "/iclaims plot [Player Name:]<Plot Name> <World Name>");
//					player.sendMessage(pluginPrefix + ChatColor.RED + "[optional] <required>");
//					return false;
//				}
//				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot"))
//				{
//					if(args.length > 1 && permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.others") && args[1].contains(":"))
//					{
//						String[] splitArray = args[1].split(":");
//						String otherPlayer = splitArray[0];
//						String plotName = splitArray[1];
//						String worldName = args[2];
//						if(DEBUGGING)
//						{
//							log.debug("Teleporting to another players plot.");
//							log.debug("args[1]: " + args[1]);
//							log.debug("Other Player: " + splitArray[0]);
//							log.debug("Other players plot name: " + splitArray[1]);
//						}
//						icUtils.getOtherPlot(player, otherPlayer, plotName, worldName);
//						return true;
//					}
//					else
//					{
//						icUtils.getPlot(player, args[1], args[2]);
//						return true;
//					}
//				}
//				else
//				{
//					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
//					return false;
//				}
//
//			}
////			else if(args[0].equalsIgnoreCase("reset"))
////			{
////				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.regen"))
////				{
////					icUtils.regeneratePlot(player);
////					return true;
////				}
////				else
////				{
////					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
////					return false;
////				}
////			}
//			else if(args[0].equalsIgnoreCase("get"))
//			{
//				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.get"))
//				{
//					ChunkGenerator cg = player.getWorld().getGenerator();
//					if(cg instanceof InfinitePlotsGenerator)
//					{
//						int plotSize = ((InfinitePlotsGenerator)cg).getPlotSize();
//						icUtils.retrievePlot(player.getWorld(), player, plotHeight, plotSize);
//						return true;
//					}
//					else
//					{
//						player.sendMessage(pluginPrefix + "Please enter a InfinitePlots world to get a plot.");
//						return false;
//					}
//				}
//				else
//				{
//					player.sendMessage(pluginPrefix + ChatColor.RED + "You do not have permission to do this.");
//					player.sendMessage(pluginPrefix + ChatColor.GREEN + "infiniteclaims.plot.get");
//					return false;
//				}
//			}
//			else if(args[0].equalsIgnoreCase("list"))
//			{
//				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.list"))
//				{
//					if(args.length < 2)
//					{
//						icUtils.listPlots(player, player.getLocation().getWorld().getName());
//						return true;
////						return false;
//					}
//					else if(args.length == 2)
//					{
//						icUtils.listPlots(player, args[1]);
//						return true;
//					}
//					else
//					{
//						player.sendMessage(pluginPrefix + "Correct usage:" + ChatColor.YELLOW + "/iclaims list <World Name>");
//						return false;
//					}
//				}
//				else
//				{
//					player.sendMessage(pluginPrefix + ChatColor.RED + "You do not have permission to do this.");
//					player.sendMessage(pluginPrefix + ChatColor.GREEN + "infiniteclaims.plot.list");
//					return false;
//				}
//			}
//			else if(args[0].equalsIgnoreCase("addmember"))
//			{
//				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.addmember"))
//				{
//					if(args.length < 2)
//					{
//						player.sendMessage(pluginPrefix + "Correct Usage: " + ChatColor.YELLOW + "/iclaims addmember <Player Name> <Plot Name> [World Name]");
//						player.sendMessage(pluginPrefix + ChatColor.RED + "[optional] <required>");
//						return false;
//					}
//					else if(args.length > 3)
//					{
//						icUtils.addMember(player, args[1], args[2], new WorldCreator(args[3]).createWorld());
//						return true;
//					}
//					else
//					{
//						icUtils.addMember(player, args[1], args[2], player.getLocation().getWorld());
//						return true;
//					}
//				}
//				else
//				{
//					player.sendMessage(pluginPrefix + ChatColor.RED + "You do not have permission to do this.");
//					player.sendMessage(pluginPrefix + ChatColor.GREEN + "infiniteclaims.plot.addmember");
//					return false;
//				}
//			}
//			else if(args[0].equalsIgnoreCase("removemember"))
//			{
//				if(permissionManager.hasPermission(player.getName(), "infiniteclaims.plot.removemember"))
//				{
//					if(args.length > 1)
//					{
//						icUtils.removeMember(player, args[1]);
//						return true;
//					}
//					else
//					{
//						player.sendMessage(pluginPrefix + "Please provide the naem of a player to remove from your plot.");
//						return false;
//					}
//				}
//				else
//				{
//					player.sendMessage(pluginPrefix + "You do not have permission to use that command");
//					return false;
//				}
//			}
//			else if(args[0].equalsIgnoreCase("reload"))
//			{
//				this.reloadConfig();
//			}
//			else
//			{
//				player.sendMessage(pluginPrefix + "Command not found, please try again");
//			}
//		}
//		return false;
//	}
	
	
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
	
	private void registerCommands()
	{
		this.commandHandler.registerCommand(new PlotTeleportCommand(this));
		this.commandHandler.registerCommand(new HelpCommand(this));
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

	public String getPluginPrefix() {
		return pluginPrefix;
	}
}