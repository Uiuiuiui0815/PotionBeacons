package net.uiuiuiui0815.potionbeacons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;

import java.util.Objects;

public class PotionBeaconEffect {
    public final int amplifier;
    public final StatusEffect effect;

    public int getAmplifier() {
        return amplifier;
    }

    public StatusEffect getEffect() {
        return effect;
    }

    public final Codec<PotionBeaconEffect> getCodec(){
        return RecordCodecBuilder.create(instance -> instance.group(
                Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(PotionBeaconEffect::getEffect),
                Codec.INT.fieldOf("amplifier").forGetter(PotionBeaconEffect::getAmplifier)
        ).apply(instance, PotionBeaconEffect::new));
    }

    public PotionBeaconEffect(StatusEffect effect, int amplifier) {
        this.amplifier = amplifier;
        this.effect = effect;
    }

    public String toString(){
        return Objects.requireNonNull(Registries.STATUS_EFFECT.getId(effect)) + "-" + amplifier;
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
