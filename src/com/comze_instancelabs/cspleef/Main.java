package com.comze_instancelabs.cspleef;

import java.util.HashMap;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


/*
 * 
 * @author InstanceLabs
 * 
 */


public class Main extends JavaPlugin implements Listener {

	public static HashMap<Player, String> arenap = new HashMap<Player, String>();
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>();
	public static int minplayers = 2;
	public static int maxplayers = 10;
	
	public static Economy econ = null;
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	}
	
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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
    					sender.sendMessage("�2Successfully saved arena.");
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
    					sender.sendMessage("�2Successfully saved spawn.");
    				}
    			}else if(action.equalsIgnoreCase("setbounds")){
    				if(args.length > 2){
    					Player p = (Player)sender;
    					String arenaname = args[1];
    					String num = args[2];
    					if(!num.equalsIgnoreCase("1") && !num.equalsIgnoreCase("2")){
    						p.sendMessage("�4Please provide 1 or 2 as seconds argument! /cspleef setbounds arena 1/2");
    						return false;
    					}
    					getConfig().set(arenaname + ".bounds.world", p.getWorld().getName());
    					getConfig().set(arenaname + ".bounds.loc" + num + ".x", p.getLocation().getBlockX());
    					getConfig().set(arenaname + ".bounds.loc" + num + ".y", p.getLocation().getBlockY() - 1);
    					getConfig().set(arenaname + ".bounds.loc" + num + ".z", p.getLocation().getBlockZ());
    					this.saveConfig();
    					sender.sendMessage("�2Successfully saved bounds loc.");
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
    					sender.sendMessage("�2Successfully saved lobby.");
    				}
    			}else if(action.equalsIgnoreCase("leave")){
    				Player p = (Player)sender;
    				leaveArena(p, arenap.get(p));
    			}
    		}
    		return true;
    	}
    	return false;
    }

	@EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(1).toLowerCase().contains("[spleef]")){
        	if(event.getPlayer().hasPermission("cspleef.sign")){
	        	event.setLine(1, "[Spleef]");
	        	if(!event.getLine(2).equalsIgnoreCase("")){
	        		String arena = event.getLine(2);
	        		if(isValidArena(arena)){
	        			getConfig().set(arena + ".sign.world", p.getWorld().getName());
	        			getConfig().set(arena + ".sign.loc.x", event.getBlock().getLocation().getBlockX());
						getConfig().set(arena + ".sign.loc.y", event.getBlock().getLocation().getBlockY());
						getConfig().set(arena + ".sign.loc.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("�2Successfully created arena sign.");
	        		}else{
	        			p.sendMessage("�2The arena appears to be invalid (missing components or misstyped arena)!");
	        			event.getBlock().breakNaturally();
	        		}
	        		event.setLine(0, "�9[JOIN]");
	        		event.setLine(2, arena);
	        		event.setLine(3, "0/" + Integer.toString(this.maxplayers));
	        	}
        	}
        }
	}
	
	
    @EventHandler
    public void onSignUse(PlayerInteractEvent event){
    	if (event.hasBlock())
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();
	            if(s.getLine(1).equalsIgnoreCase("[spleef]")){
	            	if(s.getLine(0).equalsIgnoreCase("�9[join]")){
		            	String arena = s.getLine(2);
		            	if(isValidArena(arena)){
		            		if(!arenap.containsKey(event.getPlayer())){
		            			joinLobby(event.getPlayer(), arena);
		            			s.setLine(3, Integer.toString(this.getPlayerCountInArena(arena)) + "/" + Integer.toString(this.maxplayers));
		            			s.update();
		            			if(maxplayers / this.getPlayerCountInArena(arena) > 0.74){
		            				s.setLine(0, "�6[VIP]");
		            			}
		            		}else{
		            			event.getPlayer().sendMessage("�4You're already in a game!");
		            		}
		            		
		            		/*if(this.getPlayerCountInArena(arena) > (this.minplayers - 1)){
		                		startArena(arena);
		                	}*/
		            	}	
	            	}else if(s.getLine(0).equalsIgnoreCase("�6[vip]")){
	            		String arena = s.getLine(2);
	            		if(event.getPlayer().hasPermission("TGCSpleef.VIP")){
		            		if(isValidArena(arena)){
			            		if(!arenap.containsKey(event.getPlayer())){
			            			joinLobby(event.getPlayer(), arena);
			            			s.setLine(3, Integer.toString(this.getPlayerCountInArena(arena)) + "/" + Integer.toString(this.maxplayers));
			            			s.update();
			            		}else{
			            			event.getPlayer().sendMessage("�4You're already in a game!");
			            		}
			            		
			            		/*if(this.getPlayerCountInArena(arena) > (this.minplayers - 1)){
			                		startArena(arena);
			                	}*/	
		            		}
		            	
		            	}else{
		            		event.getPlayer().sendMessage("�4Unfortunately you are not VIP.");
		            	}
	            	}
	            	
	            }
	        }else if(event.getClickedBlock().getType() == Material.SNOW_BLOCK || event.getClickedBlock().getType() == Material.ICE || event.getClickedBlock().getType() == Material.PACKED_ICE){
	        	if(arenap.containsKey(event.getPlayer())){
	        		event.getClickedBlock().setType(Material.AIR);	
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
    	
    	Sign s = getSignFromArena(arena);
    	s.setLine(0, "�4[FULL]");
    	s.update();
    }
    
    public void resetArena(String arena){
    	Random r = new Random();
    	
    	for(Player p : arenap.keySet()){
    		if(arenap.get(p).equalsIgnoreCase(arena)){
    			leaveArena(p, arena);
    		}
    	}
    	
    	Sign s = getSignFromArena(arena);
    	s.setLine(0, "�9[JOIN]");
    	s.setLine(3, "0/" + Integer.toString(maxplayers));
    	s.update();
    	
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
				int f = r.nextInt(3);
				if(f == 0){
					c.getWorld().getBlockAt(c.getLowLoc().getBlockX() + x, c.getLowLoc().getBlockY(), c.getLowLoc().getBlockZ() + z).setType(Material.SNOW_BLOCK);
				}else if(f == 1){
					c.getWorld().getBlockAt(c.getLowLoc().getBlockX() + x, c.getLowLoc().getBlockY(), c.getLowLoc().getBlockZ() + z).setType(Material.PACKED_ICE);
				}else{
					c.getWorld().getBlockAt(c.getLowLoc().getBlockX() + x, c.getLowLoc().getBlockY(), c.getLowLoc().getBlockZ() + z).setType(Material.ICE);
				}
			}
		}
    }
    
    
    public void joinLobby(Player p, String arena){
    	pinv.put(p, p.getInventory().getContents());
    	p.sendMessage("�3You need " + Integer.toString(minplayers) + " Players to start a game! Waiting for others to join ..");
    	if(!arenap.containsKey(p)){
    		arenap.put(p, arena);
    	}
    	p.teleport(new Location(getServer().getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z")));
    	
    	if(this.getPlayerCountInArena(arena) > (this.minplayers - 1)){
    		startArena(arena);
    	}
    }
    
    
    public void joinArena(Player p, String arena){
    	if(!arenap.containsKey(p)){
    		arenap.put(p, arena);
    	}
    	p.sendMessage("�2The game has started!");
    	p.getInventory().clear();
    	p.updateInventory();
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
    	
    	if(getPlayerCountInArena(arena) < 2){
			resetArena(arena);
		}
    }
    
    public boolean isValidArena(String arena){
    	if(getConfig().isSet(arena + ".name")){
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
    
    public Sign getSignFromArena(String arena){
		Location b_ = new Location(getServer().getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getInt(arena + ".sign.loc.x"), getConfig().getInt(arena + ".sign.loc.y"), getConfig().getInt(arena + ".sign.loc.z"));
    	BlockState bs = b_.getBlock().getState();
    	Sign s_ = null;
    	if(bs instanceof Sign){
    		s_ = (Sign)bs;
    	}else{
    		getLogger().info("Could not find sign: " + bs.getBlock().toString());
    	}
		return s_;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(arenap.containsKey(event.getPlayer())){
			Player p = event.getPlayer();
			String arena = arenap.get(p);
			Location spawn = new Location(getServer().getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.loc.x"), getConfig().getInt(arena + ".spawn.loc.y"), getConfig().getInt(arena + ".spawn.loc.z"));
			if(p.getLocation().getBlockY() + 1 < spawn.getBlockY()){
				leaveArena(p, arena);
				p.sendMessage("�4You lost!");
				if(getPlayerCountInArena(arena) < 2){
					for(Player p_ : arenap.keySet()){
						if(arenap.get(p_).equalsIgnoreCase(arena)){
							// last man standing
							EconomyResponse r = econ.depositPlayer(p_.getName(), 50D); //getConfig().getDouble("config.winning_reward"));
	            			if(!r.transactionSuccess()) {
	            				p_.sendMessage(String.format("An error occured: %s", r.errorMessage));
	                        }
	            			p_.sendMessage("�2You won!");
						}
					}
					resetArena(arena);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(arenap.containsKey(event.getPlayer())){
			if(event.getBlock().getType() != Material.SNOW_BLOCK && event.getBlock().getType() != Material.ICE && event.getBlock().getType() != Material.PACKED_ICE){
				event.setCancelled(true);
			}
		}
	}
}

