package me.hammale.kit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
	
	int timerId;
	
	Random ran = new Random();
	
	boolean night = false;
	
	public HashSet<MovingVan> vans = new HashSet<MovingVan>();
	public HashSet<String> hasKit = new HashSet<String>();

	public HashSet<String> invince = new HashSet<String>();
	
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
		startTimer();
	}	
	
	private void resetTime(){
		getServer().getScheduler().cancelTask(timerId);
		startTimer();
	}
	
	private void startTimer() {
		for(World w : getServer().getWorlds()){
			w.setTime(0L);
		}
		timerId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			public void run() {
		       if(night){
		    	   for(MovingVan van : vans){
		    		   for(int i : config.getIntegerList("Kits." + van.getName() + ".PotionEffects.Night")){
		    			   for(String s : van.users){
		    				   if(getServer().getPlayer(s) != null){
		    					   removeAllPotionEffects(getServer().getPlayer(s));
		    					   getServer().getPlayer(s).addPotionEffect(new PotionEffect(PotionEffectType.getById(i), 999999999, 1));
		    				   }
		    			   }
						}
		    	   }
		    	   night = false;
		       }else{
		    	   for(MovingVan van : vans){
		    		   for(int i : config.getIntegerList("Kits." + van.getName() + ".PotionEffects.Day")){
		    			   for(String s : van.users){
		    				   if(getServer().getPlayer(s) != null){
		    					   removeAllPotionEffects(getServer().getPlayer(s));
		    					   getServer().getPlayer(s).addPotionEffect(new PotionEffect(PotionEffectType.getById(i), 999999999, 1));
		    				   }
		    			   }
						}
		    	   }
		    	   night = true;
		       }
			}
		}, 0L, 12000L);
	}

	private void handleConfig() {
		File f = new File("plugins/PribKits/plugin.yml");
		if (!f.exists()) {
			config.options().copyDefaults(false);
			config.addDefault("Respawn", "world,1,1,1");
			String[] tmpRespawn = new String[]{"world,2,2,2", "world,3,3,3"};
			config.addDefault("RandomSpawns", tmpRespawn);
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
		if(!(sender instanceof Player)){
			sender.sendMessage("Players only please!");
			return true;
		}
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("sync")
				&& args.length > 0
				&& args[0].equalsIgnoreCase("time")
				&& p.isOp()){
			p.sendMessage(ChatColor.GREEN + "Time sync'd!");
			resetTime();
		}
		if(cmd.getName().equalsIgnoreCase("kit")){
			if(args.length == 1
					&& !args[0].equalsIgnoreCase("list")){
				for(MovingVan van : vans){
					if(van.getName().equalsIgnoreCase(args[0])){
						van.users.add(p.getName());
						write();
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
						hasKit.add(p.getName());
						if(invince.contains(p.getName())){
							invince.remove(p.getName());
						}
						tpRandom(p);
						p.sendMessage(ChatColor.GREEN + "Kit recieved!");
						return true;
					}
				}
				p.sendMessage(ChatColor.RED + "Kit not found!");
			}else if(args.length == 1
					&& args[0].equalsIgnoreCase("list")){
				p.sendMessage(ChatColor.DARK_GREEN + "---- " + ChatColor.GREEN + "KITS" + ChatColor.DARK_GREEN + " ----");
				for(MovingVan van : vans){
					p.sendMessage(ChatColor.BLUE + van.getName());
				}
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
	
	private void tpRandom(Player p) {
		int random = ran.nextInt(config.getStringList("RandomSpawns").size()+1);
		if(random == 0){
			random = 1;
		}
		String spawn = config.getStringList("RandomSpawns").get(random);
		String[] split = spawn.split(",");
		p.teleport(new Location(getServer().getWorld(split[0]), Double.parseDouble(split[1]),  Double.parseDouble(split[2]),  Double.parseDouble(split[3])));
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

	public MovingVan getVan(Player p) {
		for(MovingVan van : vans){
			if(van.users.contains(p.getName())){
				return van;
			}
		}
		return null;
	}
	
}