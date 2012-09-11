package me.hammale.kit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class kit extends JavaPlugin {
	
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public HashSet<MovingVan> vans = new HashSet<MovingVan>();
	
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = getDescription();		
		getServer().getPluginManager().registerEvents(new listener(this), this);
		makeFolder();
		this.logger.info(pdfFile.getName() + ", Version "
				+ pdfFile.getVersion() + ", Has Been Enabled!");
	}	
	
	private void makeFolder() {
		File f = new File("plugins/PribKits");
		if(!f.exists()){
			f.mkdir();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender instanceof Player
				&& cmd.getName().equalsIgnoreCase("kit")){
			Player p = (Player) sender;	
			if(args.length == 1){
				for(MovingVan van : vans){
					if(van.getName().equalsIgnoreCase(args[0])){
						p.getInventory().clear();
						removeAllPotionEffects(p);
						p.getInventory().setContents(van.unboxContents());
						p.getInventory().setArmorContents(van.unboxArmour());
						p.addPotionEffects(van.getPotEffects());
						for(PotionEffect pot : van.getPotEffects()){
							p.addPotionEffect(new PotionEffect(pot.getType(), 999999999, pot.getAmplifier()));
						}
					}
				}
			}else if(args.length == 2
					&& p.isOp()){
				if(args[0].equalsIgnoreCase("set")){
					vans.add(new MovingVan(args[1], p.getInventory(), p.getActivePotionEffects()));
				}
			}else if(args.length == 3
					&& p.isOp()){
				
			}
		}
		return true;
	}	
	
	public void removeAllPotionEffects(Player p) {
		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
	}
	
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info(pdfFile.getName() + ", Version "
				+ pdfFile.getVersion() + ", Has Been Diabled!");
	}
	
}
