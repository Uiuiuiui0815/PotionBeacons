package net.uiuiuiui0815.potionbeacons;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionBeaconEffect {
    private final int amplifier;
    private final StatusEffect statusEffect;

    public PotionBeaconEffect(int amplifier, StatusEffect statusEffect) {
        this.amplifier = amplifier;
        this.statusEffect = statusEffect;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public StatusEffectInstance createStatusEffectInstance(int duriation){
        return new StatusEffectInstance(getStatusEffect(), duriation, getAmplifier(), true, true);
    }
}
