package me.hammale.kit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class kit extends JavaPlugin {
	
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public Location respawn;
	
	public FileConfiguration config;
	
	public HashSet<MovingVan> vans = new HashSet<MovingVan>();
	public HashSet<String> hasKit = new HashSet<String>();
	
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = getDescription();		
		getServer().getPluginManager().registerEvents(new listener(this), this);
		config = getConfig();
		makeFolder();
		handleConfig();
		read();
		this.logger.info(pdfFile.getName() + ", Version "
				+ pdfFile.getVersion() + ", Has Been Enabled!");
	}	
	
	private void handleConfig() {
		File f = new File("plugins/PribKits/plugin.yml");
		if (!f.exists()) {
			config.options().copyDefaults(false);
			config.addDefault("Respawn", "world,1,1,1");
			config.options().copyDefaults(true);
			saveConfig();
		}
		readConfig();
	}

	private void readConfig() {
		String[] loc = config.getString("Respawn").split(",");
		respawn = new Location(getServer().getWorld(loc[0]),Integer.parseInt(loc[1]),Integer.parseInt(loc[2]),Integer.parseInt(loc[3]));
	}

	private void makeFolder() {
		File f = new File("plugins/PribKits");
		if(!f.exists()){
			f.mkdir();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender instanceof Player
				&& cmd.getName().equalsIgnoreCase("kit")){
			Player p = (Player) sender;	
			if(args.length == 1){
				for(MovingVan van : vans){
					if(van.getName().equalsIgnoreCase(args[0])){
						if(hasKit.contains(p.getName())){
							p.sendMessage(ChatColor.RED + "You already have a kit!");
							return true;
						}
						if(!getConfig().getString("Kits." + args[0] + ".Perm").equalsIgnoreCase("NA")
								&& !p.hasPermission(getConfig().getString("Kits." + args[0] + ".Perm"))){
							p.sendMessage(ChatColor.RED + "You don't have permission!");
							return true;
						}
						p.getInventory().clear();
						removeAllPotionEffects(p);
						//p.getInventory().setContents(van.unboxContents());
						//p.getInventory().setArmorContents(van.unboxArmour());
						//p.addPotionEffects(van.getPotEffects());
						
						for(String s : getConfig().getStringList("Kits." + args[0] + ".Items")){
							String[] tmp = s.split(":");
							if(Short.parseShort(tmp[2]) == 0){
								p.getInventory().addItem(new ItemStack(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])));
							}else{
								p.getInventory().addItem(new ItemStack(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Short.parseShort(tmp[2])));
							}
						}
						p.getInventory().setHelmet(new ItemStack(config.getInt("Kits." + args[0] + ".Armor.Head")));
						p.getInventory().setChestplate(new ItemStack(config.getInt("Kits." + args[0] + ".Armor.Chest")));
						p.getInventory().setLeggings(new ItemStack(config.getInt("Kits." + args[0] + ".Armor.Legs")));
						p.getInventory().setBoots(new ItemStack(config.getInt("Kits." + args[0] + ".Armor.Boots")));
						
						if(p.getWorld().getTime() < 12000){
							for(int i : config.getIntegerList("Kits." + args[0] + ".PotionEffects.Day")){
								p.addPotionEffect(new PotionEffect(PotionEffectType.getById(i), 999999999, 1));
							}
						}else{
							for(int i : config.getIntegerList("Kits." + args[0] + ".PotionEffects.Night")){
								p.addPotionEffect(new PotionEffect(PotionEffectType.getById(i), 999999999, 1));
							}
						}
						p.sendMessage(ChatColor.GREEN + "Kit recieved!");
						hasKit.add(p.getName());
						return true;
					}
				}
				p.sendMessage(ChatColor.RED + "Kit not found!");
			}else if(args.length == 2
					&& p.isOp()){
				if(args[0].equalsIgnoreCase("set")){
					MovingVan tmp = new MovingVan(args[1], p.getInventory(), p.getActivePotionEffects(), "NA");
					checkKit(tmp);
					vans.add(tmp);
					writeConfig(tmp, p, args[1], "NA");
					write();
					p.getInventory().clear();
					p.getInventory().setHelmet(new ItemStack(0));
					p.getInventory().setChestplate(new ItemStack(0));
					p.getInventory().setLeggings(new ItemStack(0));
					p.getInventory().setBoots(new ItemStack(0));
					removeAllPotionEffects(p);
					p.sendMessage(ChatColor.GREEN + "Kit set!");					
				}
			}else if(args.length == 3
					&& p.isOp()){
				if(args[0].equalsIgnoreCase("set")){
					MovingVan tmp = new MovingVan(args[1], p.getInventory(), p.getActivePotionEffects(), args[2]);
					checkKit(tmp);
					vans.add(tmp);
					writeConfig(tmp, p, args[1], args[2]);
					write();
					p.getInventory().clear();
					p.getInventory().setHelmet(new ItemStack(0));
					p.getInventory().setChestplate(new ItemStack(0));
					p.getInventory().setLeggings(new ItemStack(0));
					p.getInventory().setBoots(new ItemStack(0));
					removeAllPotionEffects(p);
					p.sendMessage(ChatColor.GREEN + "Kit set!");
				}
			}
		}
		return true;
	}
	
	private void writeConfig(MovingVan van, Player p, String name, String perm) {
		ArrayList<String> tmpList = new ArrayList<String>();
		for(ItemStack is : Arrays.asList(p.getInventory().getContents())){
			if(is != null){
				tmpList.add(new String(is.getTypeId() + ":" + is.getAmount() + ":" + is.getDurability()));
			}
		}
		config.set("Kits." + name + ".Perm", perm);
		config.set("Kits." + name + ".Items", tmpList.toArray());
		if(p.getInventory().getHelmet() != null){
			config.set("Kits." + name + ".Armor.Head", p.getInventory().getHelmet().getTypeId());
		}else{
			config.set("Kits." + name + ".Armor.Head", 0);
		}
		if(p.getInventory().getChestplate() != null){
			config.set("Kits." + name + ".Armor.Chest", p.getInventory().getChestplate().getTypeId());
		}else{
			config.set("Kits." + name + ".Armor.Chest", 0);
		}
		if(p.getInventory().getLeggings() != null){
			config.set("Kits." + name + ".Armor.Legs", p.getInventory().getLeggings().getTypeId());
		}else{
			config.set("Kits." + name + ".Armor.Legs", 0);
		}
		if(p.getInventory().getBoots() != null){
			config.set("Kits." + name + ".Armor.Boots", p.getInventory().getBoots().getTypeId());
		}else{
			config.set("Kits." + name + ".Armor.Boots", 0);
		}
		ArrayList<Integer> tmpPots = new ArrayList<Integer>();
		for(PotionEffect pot : p.getActivePotionEffects()){
			tmpPots.add(pot.getType().getId());
		}
		ArrayList<Integer> tmpInts = new ArrayList<Integer>();
		for(PotionEffect pot : p.getActivePotionEffects()){
			tmpInts.add(pot.getType().getId());
		}
		config.set("Kits." + name + ".PotionEffects.Day", tmpInts.toArray());
		config.set("Kits." + name + ".PotionEffects.Night", tmpInts.toArray());
		saveConfig();
		reloadConfig();
		config = getConfig();
	}

	public void checkKit(MovingVan van){
		MovingVan tmp = null;
		for(MovingVan v : vans){
			if(v.getName().equalsIgnoreCase(van.getName())){
				tmp = v;
			}
		}
		if(tmp != null){
			vans.remove(tmp);
		}
	}
	
	public void write(){
		try{
			File f = new File("plugins/PribKits/kits.dat");
			if(f.exists()){
				f.delete();				
			}
			f.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(vans);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void read(){
		try{
			File f = new File("plugins/PribKits/kits.dat");
			if(!f.exists()){
				return;
			}
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			vans = (HashSet<MovingVan>) ois.readObject();
		}catch(Exception e){
			e.printStackTrace();
		}
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

	public Location getRespawn() {
		return respawn;
	}
	
}