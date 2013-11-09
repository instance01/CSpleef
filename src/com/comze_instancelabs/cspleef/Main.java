package com.comze_instancelabs.cspleef;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * 
 * @author InstanceLabs
 * 
 */


public class Main extends JavaPlugin implements Listener {

	public static HashMap<Player, String> arenap = new HashMap<Player, String>();
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	}
	
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("cspleef")){
    		if(args.length > 0){
    			String action = args[0];
    			if(action.equalsIgnoreCase("createarena")){
    				// create arena
    				if(args.length > 1){
    					String arenaname = args[1];
    					getConfig().set(arenaname + ".name", arenaname);
    					this.saveConfig();
    				}
    			}else if(action.equalsIgnoreCase("setspawn")){
    				if(args.length > 1){
    					Player p = (Player)sender;
    					String arenaname = args[1];
    					getConfig().set(arenaname + ".spawn.world", p.getWorld().getName());
    					getConfig().set(arenaname + ".spawn.loc.x", p.getLocation().getBlockX());
    					getConfig().set(arenaname + ".spawn.loc.y", p.getLocation().getBlockY());
    					getConfig().set(arenaname + ".spawn.loc.z", p.getLocation().getBlockZ());
    					this.saveConfig();
    				}
    			}else if(action.equalsIgnoreCase("setlobby")){
    				if(args.length > 1){
    					Player p = (Player)sender;
    					String arenaname = args[1];
    					getConfig().set(arenaname + ".lobby.world", p.getWorld().getName());
    					getConfig().set(arenaname + ".lobby.loc.x", p.getLocation().getBlockX());
    					getConfig().set(arenaname + ".lobby.loc.y", p.getLocation().getBlockY());
    					getConfig().set(arenaname + ".lobby.loc.z", p.getLocation().getBlockZ());
    					this.saveConfig();
    				}
    			}else if(action.equalsIgnoreCase("leave")){
    				Player p = (Player)sender;
    				leaveArena(p, arenap.get(p));
    			}
    		}
    	}
    	return false;
    }


    @EventHandler
    public void onSignUse(PlayerInteractEvent event){
    	if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();
	            if(s.getLine(0).equalsIgnoreCase("[spleef]")){
	            	if(isValidArena(s.getLine(1))){
	            		joinArena(event.getPlayer(), s.getLine(1));
	            	}
	            }
	        }
	    }
    }
    
    public void joinArena(Player p, String arena){
    	if(!arenap.containsKey(p)){
    		arenap.put(p, arena);
    	}
    	p.teleport(new Location(getServer().getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".spawn.loc.z")));
    }
    
    public void leaveArena(Player p, String arena){
    	p.teleport(new Location(getServer().getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z")));
    	arenap.remove(p);
    }
    
    public boolean isValidArena(String arena){
    	if(getConfig().isSet(arena + "name")){
    		return true;
    	}
    	return false;
    }
}

