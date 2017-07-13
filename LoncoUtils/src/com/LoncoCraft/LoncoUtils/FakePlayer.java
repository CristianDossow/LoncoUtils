package com.LoncoCraft.LoncoUtils;



import java.util.UUID;

import org.bukkit.Location;

public class FakePlayer {
UUID uuid;
Location location;

public FakePlayer(UUID uuid, Location location) {
	super();
	this.uuid = uuid;
	this.location = location;
}

public UUID getUuid() {
	return uuid;
}

public void setUuid(UUID uuid) {
	this.uuid = uuid;
}

public Location getLocation() {
	return location;
}

public void setLocation(Location location) {
	this.location = location;
}



}
