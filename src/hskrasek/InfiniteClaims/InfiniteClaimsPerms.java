package hskrasek.InfiniteClaims;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pneumaticraft.commandhandler.PermissionsInterface;

public class InfiniteClaimsPerms implements PermissionsInterface
{
	InfiniteClaims plugin;
	
	public InfiniteClaimsPerms(InfiniteClaims instance)
	{
		this.plugin = instance;
	}
	
	public boolean hasPermission(CommandSender sender, String node, boolean isOpRequired) 
	{
		if(node == null)
		{
			return false;
		}
		if(node.equals(""))
		{
			return true;
		}
		
		return hasPermission(sender, node);
	}

	private boolean hasPermission(CommandSender sender, String node)
	{
//		Player player = (Player)sender;
		boolean hasPerm = sender.hasPermission(node);
		
		return hasPerm;
	}
	
	public boolean hasAnyPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) 
	{
		for(String node : allPermissionStrings)
		{
			if(this.hasPermission(sender, node, opRequired))
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasAllPermission(CommandSender sender, List<String> allPermissionStrings, boolean opRequired) 
	{
		for(String node : allPermissionStrings)
		{
			if(!sender.hasPermission(node))
			{
				return false;
			}
		}
		return true;
	}
	
}
