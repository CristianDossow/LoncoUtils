package com.LoncoCraft.LoncoUtils;

import java.util.UUID;

public class PlayerReport {
	int EntityId;
	int TotalFlyCasualties;
	UUID uuid;
	
	
	
	public PlayerReport(int entityId, UUID uuid) {
	
		EntityId = entityId;
		this.uuid = uuid;
	}
	
	public int getEntityId() {
		return EntityId;
	}
	public void setEntityId(int entityId) {
		EntityId = entityId;
	}
	public int getTotalFlyCasualties() {
		return TotalFlyCasualties;
	}
	public void setTotalFlyCasualties(int totalFlyCasualties) {
		TotalFlyCasualties = totalFlyCasualties;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
}
