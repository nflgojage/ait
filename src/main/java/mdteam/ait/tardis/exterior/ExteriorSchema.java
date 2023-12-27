package mdteam.ait.tardis.exterior;

import com.google.gson.*;
import mdteam.ait.core.AITDoors;
import mdteam.ait.core.AITExteriors;
import mdteam.ait.core.AITSounds;
import mdteam.ait.core.sounds.MatSound;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.variant.door.CapsuleDoorVariant;
import mdteam.ait.tardis.variant.door.DoorSchema;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.lang.reflect.Type;

public abstract class ExteriorSchema {
    private final Identifier id;
    private final String name;

    protected ExteriorSchema(Identifier id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() == null) return false;

        ExteriorSchema that = (ExteriorSchema) o;

        return id.equals(that.id);
    }

    public Identifier id() {
        return this.id;
    }
    public String name() { return this.name; }

    @Override
    public String toString() {
        return this.name();
    }

    public MatSound getSound(TardisTravel.State state) {
        return switch (state) {
            case LANDED, CRASH -> AITSounds.LANDED_ANIM;
            case FLIGHT -> AITSounds.FLIGHT_ANIM;
            case DEMAT -> AITSounds.DEMAT_ANIM;
            case MAT -> AITSounds.MAT_ANIM;
        };
    }

    public static Object serializer() {
        return new Serializer();
    }

    private static class Serializer implements JsonSerializer<ExteriorSchema>, JsonDeserializer<ExteriorSchema> {

        @Override
        public ExteriorSchema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Identifier id;

            try {
                id = new Identifier(json.getAsJsonPrimitive().getAsString());
            } catch (InvalidIdentifierException e) {
                id = CapsuleExterior.REFERENCE;
            }

            return AITExteriors.get(id);
        }

        @Override
        public JsonElement serialize(ExteriorSchema src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.id().toString());
        }
    }
}
