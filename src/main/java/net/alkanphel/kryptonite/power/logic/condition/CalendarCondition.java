package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.Optional;

public record CalendarCondition(@Nullable Value year, @Nullable Value month, @Nullable Value day, @Nullable Value hour, @Nullable Value minute, @Nullable Value second) implements Condition {

    public static final MapCodec<CalendarCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("year").forGetter(c -> Optional.ofNullable(c.year())),
            Value.CODEC.optionalFieldOf("month").forGetter(c -> Optional.ofNullable(c.month())),
            Value.CODEC.optionalFieldOf("day").forGetter(c -> Optional.ofNullable(c.day())),
            Value.CODEC.optionalFieldOf("hour").forGetter(c -> Optional.ofNullable(c.hour())),
            Value.CODEC.optionalFieldOf("minute").forGetter(c -> Optional.ofNullable(c.minute())),
            Value.CODEC.optionalFieldOf("second").forGetter(c -> Optional.ofNullable(c.second()))
    ).apply(instance, (year, month, day, hour, minute, second) -> new CalendarCondition(
            year.orElse(null), month.orElse(null), day.orElse(null), hour.orElse(null), minute.orElse(null), second.orElse(null)
    )));

    public static final StreamCodec<RegistryFriendlyByteBuf, CalendarCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), c -> Optional.ofNullable(c.year()),
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), c -> Optional.ofNullable(c.month()),
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), c -> Optional.ofNullable(c.day()),
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), c -> Optional.ofNullable(c.hour()),
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), c -> Optional.ofNullable(c.minute()),
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC)), c -> Optional.ofNullable(c.second()),
            (year, month, day, hour, minute, second) -> new CalendarCondition(year.orElse(null), month.orElse(null), day.orElse(null), hour.orElse(null), minute.orElse(null), second.orElse(null))
    );

    @Override
    public boolean test(DataContext context) {
        var now = LocalDateTime.now();
        if (year != null && Math.clamp(year.getAsInt(context), 1, Year.MAX_VALUE) != now.getYear()) return false;
        if (month != null && Math.clamp(month.getAsInt(context), 1, 12) != now.getMonthValue()) return false;
        if (day != null && Math.clamp(day.getAsInt(context), 1, 31) != now.getDayOfMonth()) return false;
        if (hour != null && Math.clamp(hour.getAsInt(context), 0, 23) != now.getHour()) return false;
        if (minute != null && Math.clamp(minute.getAsInt(context), 0, 59) != now.getMinute()) return false;
        if (second != null && Math.clamp(second.getAsInt(context), 0, 59) != now.getSecond()) return false;
        return true;
    }

    @Override
    public ConditionSerializer<CalendarCondition> getSerializer() {
        return KryptoniteConditionSerializers.CALENDAR.get();
    }

    public static class Serializer extends ConditionSerializer<CalendarCondition> {

        @Override
        public MapCodec<CalendarCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, CalendarCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Calendar")
                    .setDescription("Checks the current real-life date and time. Omitted fields are ignored.")
                    .addOptional("year", SettingType.intRange(1, Year.MAX_VALUE), "The year to match.")
                    .addOptional("month", SettingType.intRange(1, Month.values().length), "The month to match.")
                    .addOptional("day", SettingType.intRange(1, 31), "The day to match.")
                    .addOptional("hour", SettingType.intRange(0, LocalTime.MAX.getHour()), "The hour to match.")
                    .addOptional("minute", SettingType.intRange(0, LocalTime.MAX.getMinute()), "The minute to match.")
                    .addOptional("second", SettingType.intRange(0, LocalTime.MAX.getSecond()), "The second to match.")
                    .addExampleObject(new CalendarCondition(null, new StaticValue(7), new StaticValue(8), null, null, null));
        }
    }

}