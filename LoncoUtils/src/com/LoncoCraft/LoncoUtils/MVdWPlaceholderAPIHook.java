package com.LoncoCraft.LoncoUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class MVdWPlaceholderAPIHook {
    public static void hook(Main plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceHolderAPI")) {
        	
            PlaceholderAPI.registerPlaceholder(plugin, "endercd",
                    new PlaceholderReplacer() {
					
                        @Override
                        public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
                            final OfflinePlayer offp = e.getPlayer();
                            if (offp == null) {
                                return "Player is needed!";
                            }
                            
                            if(plugin.getEnderCD(e.getPlayer().getUniqueId())==0){
                            	return plugin.getPlaceholderEnderPearlCDReady();
                            }
                            
                            String EnderCD = ""+plugin.getEnderCD(e.getPlayer().getUniqueId());
                            		
                            return EnderCD;
                        }
            	}
            );
            
        }
    }
}
