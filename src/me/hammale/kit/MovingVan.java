package me.hammale.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class MovingVan implements Serializable
{
    private static final long serialVersionUID = 4679317485302321489L;
 
    private final HashMap<Integer, CardboardBox> s_armour, s_items;
 
    private String name;
    public String perm;
    
	private Collection<PotionEffectBox> potEffects = new ArrayList<PotionEffectBox>();
	public HashSet<String> users = new HashSet<String>();
    
    public MovingVan(String name, PlayerInventory inventory, Collection<PotionEffect> potEffects, String perm) {
    	this.name = name;
    	this.perm = perm;
    	for(PotionEffect pot : potEffects){
    		this.potEffects.add(new PotionEffectBox(pot));
    	}
        ItemStack[] armour, items;
        armour = inventory.getArmorContents();
        items = inventory.getContents();
     
        s_armour = new HashMap<Integer, CardboardBox>();
        s_items = new HashMap<Integer, CardboardBox>();
     
        //armour
        for(int i=0; i<armour.length; i++)
        {
            CardboardBox value = null;
            if(armour[i] != null){
                value = new CardboardBox(armour[i]);
            }
            s_armour.put(i, value);
        }
        //items
        for(int i=0; i<items.length; i++)
        {
            CardboardBox value = null;
            if(items[i] != null){
                value = new CardboardBox(items[i]);
            }
            s_items.put(i, value);
        }
	}

	public String getName(){
    	return this.name;
    }
    
	public Collection<PotionEffect> getPotEffects(){
		Collection<PotionEffect> effects = new ArrayList<PotionEffect>();
		for(PotionEffectBox pot : this.potEffects){
			effects.add(pot.unBox());
		}
		return effects;
	}
	
    /**
    * Gets the armour item stacks.
    * @return
    */
    public ItemStack[] unboxArmour()
    {
        ItemStack[] armour = new ItemStack[s_armour.size()];
        for(int i=0; i<s_armour.size(); i++)
        {
            if(s_armour.get(i) == null){
                armour[i] = null;
            }else{
                armour[i] = s_armour.get(i).unbox();
            }
        }
        return armour;
    }
 
    /**
    * Gets the Items (Contents) of the inventory.
    * @return
    */
    public ItemStack[] unboxContents()
    {
        ItemStack[] items = new ItemStack[s_items.size()];
        for(int i=0; i<s_items.size(); i++)
        {
            if(s_items.get(i) == null){
                items[i] = null;
            }else{
                items[i] = s_items.get(i).unbox();
            }
        }
        return items;
    }
 
}