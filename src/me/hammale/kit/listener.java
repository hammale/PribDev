package me.hammale.kit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class listener implements Listener {
	
	private kit plugin;

	public listener(kit plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		Player p = e.getEntity();
		if(plugin.hasKit.contains(p.getName())){
			plugin.hasKit.remove(p.getName());
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e){
		Player p = e.getPlayer();
		e.setRespawnLocation(plugin.getRespawn());
		p.sendMessage(ChatColor.YELLOW + "Please select a kit with /kit <name>");
	}
	
}
