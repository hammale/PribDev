package me.hammale.kit;

import java.io.Serializable;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectBox implements Serializable {
	
	private static final long serialVersionUID = -8941957195508535260L;
	private int apmlifier;
	private PotionType type;
	
	public enum PotionType {
		BLINDNESS,CONFUSION,DAMAGE_RESISTANCE,FAST_DIGGING,FIRE_RESISTANCE,HARM,HEAL,HUNGER,INCREASE_DAMAGE,INVISIBILITY,JUMP,NIGHT_VISION,POISON,REGENERATION,SLOW,SLOW_DIGGING,SPEED,WATER_BREATHING,WEAKNESS
	}
	
	public PotionEffectBox(PotionEffect pot) {
		this.type = PotionType.valueOf(pot.getType().getName());
		this.apmlifier = pot.getAmplifier();
	}

	public PotionEffect unBox(){
		return new PotionEffect(PotionEffectType.getByName(type.toString()), 999999999, apmlifier);
	}
	
}
