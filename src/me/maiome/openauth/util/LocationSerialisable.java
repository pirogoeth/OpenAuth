package me.maiome.openauth.util;

// bukkit
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.*;

// java
import java.util.HashMap;
import java.util.Map;

public class LocationSerialisable implements ConfigurationSerializable {

    public static LocationSerialisable deserialize(final Map<String, Object> s) {
        return new LocationSerialisable(
            (double) Double.parseDouble(s.get("x").toString()),
            (double) Double.parseDouble(s.get("y").toString()),
            (double) Double.parseDouble(s.get("z").toString()),
            (float) Float.parseFloat(s.get("pitch").toString()),
            (float) Float.parseFloat(s.get("yaw").toString()),
            (String) s.get("world")
        );
    }

    private double x, y, z;
    private float pitch, yaw;
    private String worldname;

    public LocationSerialisable(final Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.pitch = loc.getPitch();
        this.yaw = loc.getYaw();
        this.worldname = loc.getWorld().getName();
    }

    public LocationSerialisable(final double x, final double y, final double z, final float pitch, final float yaw, final String worldname) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.worldname = worldname;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> loc = new HashMap<String, Object>();
        loc.put("x", this.x);
        loc.put("y", this.y);
        loc.put("z", this.z);
        loc.put("pitch", this.pitch);
        loc.put("yaw", this.yaw);
        loc.put("world", this.worldname);

        return loc;
    }

    public Location getLocation() {
        Server s = Bukkit.getServer();
        return new Location(
            s.getWorld(this.worldname),
            this.x,
            this.y,
            this.z,
            this.pitch,
            this.yaw
        );
    }
}