package hskrasek.InfiniteClaims;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

public class InfiniteClaimsConfig 
{
	private YamlConfiguration config;
	private HashMap<String, Object> configDefaults = new HashMap<String, Object>();
	
	public InfiniteClaimsConfig(File configFile)
	{
		config = new YamlConfiguration();
		
		configDefaults.put("debugging", false);
		configDefaults.put("plots.height",20);
		configDefaults.put("signs.enabled", true);
		configDefaults.put("signs.placement", "entrance");
		configDefaults.put("signs.prefix", "Plot Owner:");
		configDefaults.put("signs.prefix-color", "black");
		configDefaults.put("signs.owner-color", "black");
		
		if(configFile.exists() == false)
		{
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
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
