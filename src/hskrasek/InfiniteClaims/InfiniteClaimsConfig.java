package hskrasek.InfiniteClaims;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

public class InfiniteClaimsConfig 
{
	private YamlConfiguration config;
	private YamlConfigurationOptions configOptions;
	private HashMap<String, Object> configDefaults = new HashMap<String, Object>();
	private InfiniteClaims plugin;
	
	public InfiniteClaimsConfig(File configFile, InfiniteClaims instance)
	{
		// TODO Add version number into configuration for easier config option additions, without wiping users current config options. Or some sort of version control.
		// TODO Finish writing config header
		plugin = instance;
		config = new YamlConfiguration();
		configOptions = config.options();
		configDefaults.put("debugging", false);
		configDefaults.put("plots.height",20);
		configDefaults.put("plots.max-plots", 1);
		configDefaults.put("signs.enabled", true);
		configDefaults.put("signs.placement", "entrance");
		configDefaults.put("signs.prefix", "Plot Owner:");
		configDefaults.put("signs.prefix-color", "black");
		configDefaults.put("signs.owner-color", "black");
		String header = "InfiniteClaims Header";
		configOptions.header(header);
		if(configFile.exists() == false)
		{
			configOptions.copyHeader(true);
			for(String key : configDefaults.keySet())
			{
				config.set(key, configDefaults.get(key));
			}
			
			try
			{
				config.save(configFile);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				config.load(configFile);
				if(!config.contains("version"))
				{
					plugin.log.info("You are running an older version of InfiniteClaims, let us do some house cleaning for the upgrade...");
					plugin.icUtils.legacyUpdate(plugin.getServer());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public boolean contains(String key)
	{
		return config.contains(key);
	}
	
	public boolean getBoolean(String key)
	{
		if(!configDefaults.containsKey(key))
		{
			return false;
		}
		
		return config.getBoolean(key, (Boolean) this.configDefaults.get(key));
	}
	
	public String getString(String key)
	{
		if(!configDefaults.containsKey(key))
		{
			return "";
		}
		
		return config.getString(key, (String)configDefaults.get(key));
	}
	
	public int getInt(String key)
	{
		if(!configDefaults.containsKey(key))
		{
			return 0;
		}
		
		return config.getInt(key, (Integer)configDefaults.get(key));
	}
	
	public double getDouble(String key)
	{
		if(!configDefaults.containsKey(key))
		{
			return 0.0;
		}
		
		return config.getDouble(key, (Double)configDefaults.get(key));
	}
}
