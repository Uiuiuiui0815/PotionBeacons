package net.uiuiuiui0815.potionbeacons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;

public record PotionBeaconEffect(StatusEffect effect, int amplifier) {

    public Codec<PotionBeaconEffect> getCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(PotionBeaconEffect::effect),
                Codec.INT.fieldOf("amplifier").forGetter(PotionBeaconEffect::amplifier)
        ).apply(instance, PotionBeaconEffect::new));
    }

    public StatusEffectInstance createStatusEffectInstance() {
        return new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(effect),340, amplifier,true,true);
    }
}
