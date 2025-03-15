package net.uiuiuiui0815.potionbeacons;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PotionBeaconEffect {
    public int amplifier;
    public StatusEffect effect;

    public PotionBeaconEffect(NbtCompound nbtCompound){
        amplifier=nbtCompound.getInt("amplifier",0);
        String s = nbtCompound.getString("effect","");
        effect = Registries.STATUS_EFFECT.get(Identifier.tryParse(s));
    }

    public PotionBeaconEffect(StatusEffect effect, int amplifier) {
        this.amplifier = amplifier;
        this.effect = effect;
    }

    public NbtCompound toNBT(){
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("effect", Registries.STATUS_EFFECT.getId(effect).toString());
        nbtCompound.putInt("amplifier", amplifier);

        return nbtCompound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PotionBeaconEffect that = (PotionBeaconEffect) o;

        if (amplifier != that.amplifier) return false;
        return Objects.equals(effect, that.effect);
    }

    public StatusEffectInstance createStatusEffectInstance() {
        return new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(effect), 340, amplifier,true,true);
    }
}
