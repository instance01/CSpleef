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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * 
 * @author InstanceLabs
 * 
 */


public class Main extends JavaPlugin implements Listener {

	public static HashMap<Player, String> arenap = new HashMap<Player, String>();
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>();
	public static int minplayers = 3;
	public static int maxplayers = 10;

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
    			}else if(action.equalsIgnoreCase("setbounds")){
    				if(args.length > 2){
    					Player p = (Player)sender;
    					String arenaname = args[1];
    					String num = args[2];
    					if(!num.equalsIgnoreCase("1") && !num.equalsIgnoreCase("2")){
    						p.sendMessage("§4Please provide 1 or 2 as seconds argument! /cspleef setbounds 1/2");
    						return false;
    					}
    					getConfig().set(arenaname + ".bounds.world", p.getWorld().getName());
    					getConfig().set(arenaname + ".bounds.loc" + num + ".x", p.getLocation().getBlockX());
    					getConfig().set(arenaname + ".bounds.loc" + num + ".y", p.getLocation().getBlockY() - 1);
    					getConfig().set(arenaname + ".bounds.loc" + num + ".z", p.getLocation().getBlockZ());
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
	            	String arena = s.getLine(1);
	            	if(isValidArena(arena)){
	            		if(!arenap.containsKey(event.getPlayer())){
	            			joinLobby(event.getPlayer(), arena);	
	            		}else{
	            			event.getPlayer().sendMessage("§4You're already in a game!");
	            		}
	            		
	            		if(this.getPlayerCountInArena(arena) > (this.minplayers - 1)){
	                		startArena(arena);
	                	}
	            	}
	            }
	        }
	    }
    }
    
    
    public void startArena(String arena){
    	for(Player p : arenap.keySet()){
    		if(arenap.get(p).equalsIgnoreCase(arena)){
    			joinArena(p, arena);
    		}
    	}
    }
    
    public void resetArena(String arena){
    	for(Player p : arenap.keySet()){
    		if(arenap.get(p).equalsIgnoreCase(arena)){
    			leaveArena(p, arena);
    		}
    	}
    	
		Location loc1 = new Location(getServer().getWorld(getConfig().getString(arena + ".bounds.world")), getConfig().getInt(arena + ".bounds.loc1.x"), getConfig().getInt(arena + ".bounds.loc1.y"), getConfig().getInt(arena + ".bounds.loc1.z"));
		Location loc2 = new Location(getServer().getWorld(getConfig().getString(arena + ".bounds.world")), getConfig().getInt(arena + ".bounds.loc2.x"), getConfig().getInt(arena + ".bounds.loc2.y"), getConfig().getInt(arena + ".bounds.loc2.z"));

		/*int x1 = loc1.getBlockX();
		int y1 = loc1.getBlockY();
		int z1 = loc1.getBlockZ();
		int x2 = loc2.getBlockX();
		int y2 = loc2.getBlockY();
		int z2 = loc2.getBlockZ();*/
		
		Cuboid c = new Cuboid(loc1, loc2);
		
		int width = c.getHighLoc().getBlockX() - c.getLowLoc().getBlockX();
		int length = c.getHighLoc().getBlockZ() - c.getLowLoc().getBlockZ();
		/*int width = x1 - x2;
		if(width < 0){
			width = -width;
		}
		int length = z1 - z2;
		if(length < 0){
			length = -length;
		}*/
		
		for(int x = 0; x <= width; x++){
			for(int z = 0; z <= length; z++){
				c.getWorld().getBlockAt(c.getLowLoc().getBlockX() + x, c.getLowLoc().getBlockY(), c.getLowLoc().getBlockZ() + z).setType(Material.SNOW_BLOCK);
			}
		}
    }
    
    
    public void joinLobby(Player p, String arena){
    	p.sendMessage("§3You need " + Integer.toString(minplayers) + " Players to start a game! Waiting for others to join ..");
    	if(!arenap.containsKey(p)){
    		arenap.put(p, arena);
    	}
    	p.teleport(new Location(getServer().getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z")));
    }
    
    
    public void joinArena(Player p, String arena){
    	pinv.put(p, p.getInventory().getContents());
    	if(!arenap.containsKey(p)){
    		arenap.put(p, arena);
    	}
    	p.teleport(new Location(getServer().getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".spawn.loc.z")));
    	p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE, 1));
    	p.updateInventory();
    }
    
    public void leaveArena(Player p, String arena){
    	p.teleport(new Location(getServer().getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z")));
    	arenap.remove(p);
    	p.getInventory().clear();
    	p.updateInventory();
    	p.getInventory().setContents(pinv.get(p));
    	p.updateInventory();
    }
    
    public boolean isValidArena(String arena){
    	if(getConfig().isSet(arena + "name")){
    		return true;
    	}
    	return false;
    }
    
    public int getPlayerCountInArena(String arena){
    	int count = 0;
    	for(Player p : arenap.keySet()){
    		if(arenap.get(p).equalsIgnoreCase(arena)){
    			count += 1;
    		}
    	}
    	return count;
    }
    
    
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(arenap.containsKey(event.getPlayer().getName())){
			Player p = event.getPlayer();
			String arena = arenap.get(p);
			Location spawn = new Location(getServer().getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".spawn.loc.z"));
			if(p.getLocation().getBlockY() + 1 < spawn.getBlockY()){
				leaveArena(p, arena);
				p.sendMessage("§4You lost!");
				if(getPlayerCountInArena(arena) < 2){
					resetArena(arena);
				}
			}
		}
	}
}

