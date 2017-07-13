package com.LoncoCraft.LoncoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class Main extends JavaPlugin implements Listener{
    // Fired when plugin is first enabled
	private ProtocolManager protocolManager;
	private int ThunderMaxDistance;
	List<FakePlayer> PlayerList=new ArrayList<FakePlayer>();
	FileConfiguration config = getConfig();
	File cfile = new File(getDataFolder(), "config.yml");
	List<PlayerReport> PlayerCheker=new ArrayList<PlayerReport>();
	
	final int tickstodrop = 40;
	int ticksdroped=0;
	int multas = 5;
	Server server;
	ConsoleCommandSender console;
	
	private HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
	
	
    @Override
    public void onEnable() {
    	
    	
    	
        config.addDefault("FlySpeedLimit", true);
        config.addDefault("NoGlobalThunder", true);
        config.addDefault("NoBowSelfDamage", true);
        config.addDefault("NoFishingRodOnPlayer", true);
        config.addDefault("FlyHackCheck", true);
        config.addDefault("NewEnchantFix", false);
        config.addDefault("Enderpearl-Cooldown-Time", 10.0);
        config.addDefault("Enderpearl-Cooldown-Enable", false);
        config.addDefault("Enderpearl-Cooldown-Message", "Aun no puedes usar este objeto");
        config.addDefault("Enderpearl-Cooldown-SendMessage", true);
        config.addDefault("ReturnEnderPearlOnCD", false);
        config.addDefault("PlaceholderEnderPearlCDReady", "Disponible");
        config.options().copyDefaults(true);
        saveConfig();
        
        server = Bukkit.getServer();
        console = server.getConsoleSender();
    	
    	getServer().getPluginManager().registerEvents(this, this);
    	
    	protocolManager = ProtocolLibrary.getProtocolManager();
    	ThunderMaxDistance=(Bukkit.getViewDistance()*16-2);
    	
    	MVdWPlaceholderAPIHook.hook(this);
    	
        this.protocolManager.addPacketListener((PacketListener)new PacketAdapter((Plugin)this, new PacketType[]{PacketType.Play.Server.NAMED_SOUND_EFFECT}){

            public void onPacketSending(PacketEvent event) {
            	if(config.getBoolean("NoGlobalThunder")){
                	if (event.getPacketType() == 
                            PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                        //event.setCancelled(true);
                        PacketContainer packet = event.getPacket();
                        Player p = event.getPlayer();
                        Sound sound=packet.getSoundEffects().read(0);
                        Sound sound2=org.bukkit.Sound.ENTITY_LIGHTNING_THUNDER;
                        Sound sound3=org.bukkit.Sound.ENTITY_LIGHTNING_IMPACT;
                        //String soundName = packet.getStrings().read(0);
                        if (sound.compareTo(sound2)==1||sound.compareTo(sound3)==1) {
                            int x = (Integer)packet.getIntegers().read(0) / 8;
                            int z = (Integer)packet.getIntegers().read(2) / 8;
                            int distance = distanceBetweenPoints(x, p.getLocation().getBlockX(), z, p.getLocation().getBlockZ());
                            if (distance > ThunderMaxDistance) {
                            	//Bukkit.broadcastMessage("X="+x+" Z="+z+" distance="+distance+" Player="+p.getName()+ " True");
                            	event.setCancelled(true);
                            }else{
                            	//Bukkit.broadcastMessage("test4");
                            	//Bukkit.broadcastMessage("X="+x+" Z="+z+" distance="+distance+" Player="+p.getName()+ " False");
                            }
                        }
                    }
            	}
            }
        });
        
    }
    
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
    	if(config.getBoolean("FlyHackCheck") && ticksdroped > tickstodrop ){
    		ticksdroped=0;
    		
        	Player p = event.getPlayer();
        	Vector pos1= new Vector(event.getFrom().getX(),0,event.getFrom().getZ());
        	Vector pos2= new Vector(event.getTo().getX(),0,event.getTo().getZ());
        	
        	
        	if(event.getFrom().getWorld().equals(event.getTo().getWorld()) && !p.hasPermission("LoncoUtils.admin")){
                if(pos1.distance(pos2)>0.8){
                	p.setFlySpeed((float) 0.060);
    		    }else{
    		    	p.setFlySpeed((float) 0.1);
    		    }
                if( pos1.distance(pos2)>1.2 && !p.isFlying() && !p.hasPotionEffect(PotionEffectType.SPEED)){
                	//Bukkit.broadcast("Multa","");
                	boolean found=false;
                	for(PlayerReport PR : PlayerCheker){
                		if(PR.uuid.equals(p.getUniqueId())){
                			found=true;
                    		PR.setTotalFlyCasualties(PR.getTotalFlyCasualties()+1);
                    		if(PR.getTotalFlyCasualties()>multas){
                    			console.sendMessage(ChatColor.RED + "LoncoUtils: El jugador "+p.getName()+" ("+p.getDisplayName()+") es sospechoso de Fly Hack");
                    			for(Player admins : Bukkit.getOnlinePlayers()){
                    				if(admins.hasPermission("LoncoUtils.mod"))
                    					admins.sendMessage("LoncoUtils: El jugador "+p.getName()+" ("+p.getDisplayName()+") es sospechoso de Fly Hack");
                    			}
                    			PR.setTotalFlyCasualties(0);
                    		}
                		}
                	}
                    if(!found) {
                		PlayerCheker.add(new PlayerReport(p.getEntityId(), p.getUniqueId()));
        		    }
                }

                
        	}
    	}
    	else{
    		ticksdroped=ticksdroped+1;
    	}
    	

    	
    }
    
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }
    public int distanceBetweenPoints(int x1, int x2, int y1, int y2) {
        return (int)Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
    
    
    @EventHandler
    public void onPrepareAnvilEvent(PrepareAnvilEvent e){
    	if(config.getBoolean("NewEnchantFix")) {
    		
    	org.bukkit.inventory.ItemStack item1 = e.getInventory().getItem(0);
    	//org.bukkit.inventory.ItemStack item2 = e.getInventory().getItem(1);
    	org.bukkit.inventory.ItemStack item3 = e.getResult();
   
    	if(item1 != null && item3 != null){
            if(!item1.containsEnchantment(Enchantment.FROST_WALKER) && item3.containsEnchantment(Enchantment.FROST_WALKER)){
            	ItemMeta im = item3.getItemMeta();
            	
            	List<String> temlore = new ArrayList<String>();
            	if(item3.getItemMeta().getLore()!=null){
            		temlore.addAll(item3.getItemMeta().getLore());
            	}
            	
            	temlore.add(ChatColor.GRAY+"Paso helado "+Utils.toRoman(item3.getEnchantmentLevel(Enchantment.FROST_WALKER)));
            	item3.getItemMeta().setLore(temlore);
            	im.setLore(temlore);
            	item3.setItemMeta(im); 
            }
            
            if(!item1.containsEnchantment(Enchantment.MENDING) && item3.containsEnchantment(Enchantment.MENDING)){
            	
            	ItemMeta im = item3.getItemMeta();
            	
            	List<String> temlore = new ArrayList<String>();
            	if(item3.getItemMeta().getLore()!=null){
            		temlore.addAll(item3.getItemMeta().getLore());
            	}
            	temlore.add(ChatColor.GRAY+"Reparaci�n");
            	item3.getItemMeta().setLore(temlore);
            	im.setLore(temlore);
            	item3.setItemMeta(im); 
            }
    	}
    	
    	}
  
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
    	if(config.getBoolean("NoBowSelfDamage")){
    		if(event.getDamager() != null)
    			if(event.getDamager() instanceof Projectile)
    			if(((Projectile)event.getDamager()).getShooter() != null)
		        	if(event.getDamager().getType().equals(EntityType.ARROW)){
		                if(((Projectile)event.getDamager()).getShooter().equals(event.getEntity())){
		                	event.setCancelled(true);
		                }
		        	}
    	}
    }
    
    
    
    @EventHandler
    public void onPlayerHitFishingrod(final PlayerFishEvent event) {
    	if(config.getBoolean("NoFishingRodOnPlayer")){
            final Player player = event.getPlayer();
            if (event.getCaught() instanceof Player) {
                if (player.getEquipment().getItemInMainHand().getType() == Material.FISHING_ROD) {
            	    event.setCancelled(true);
                    //player.sendMessage(ChatColor.RED + "GET OVER HERE!");
                    //caught.teleport(player.getLocation());
                }
            }
    	}
    }
    
    
    @EventHandler()
    public void enderPearlThrown(PlayerTeleportEvent event) {
    	if (!event.getPlayer().hasPermission("LoncoUtils.Enderpear.Bypass") && config.getBoolean("Enderpearl-Cooldown-Enable") && (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT))) {
    		Long time = System.currentTimeMillis();
    		Player player = event.getPlayer();
    		if(cooldowns.containsKey(player.getUniqueId())){
    			
    			Long lastUsage =cooldowns.get(player.getUniqueId());
    			if(lastUsage + config.getDouble("Enderpearl-Cooldown-Time")*1000<time){
    				cooldowns.put(player.getUniqueId(), time) ;
    			}
    			else{

					if(config.getBoolean("Enderpearl-Cooldown-SendMessage")){
    					player.sendMessage(config.getString("Enderpearl-Cooldown-Message"));
    				}
					
					if(config.getBoolean("ReturnEnderPearlOnCD") && event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)){
						ItemStack perlaender = new ItemStack(Material.ENDER_PEARL,1);
						player.getInventory().addItem(perlaender);
	    				event.setCancelled(true);
						
					}
					
    			}
    		}
    		else{
    			cooldowns.put(player.getUniqueId(), time) ;
    		}
    	}
    }
 
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().toLowerCase().equalsIgnoreCase("loncoreload") && sender.hasPermission("LoncoUtils.admin")){ //Si el jugador ha escrito /basic entonces haga lo siguiente...
            config = YamlConfiguration.loadConfiguration(cfile);
    		sender.sendMessage("Datos Recargados Correctamente");
    		return true;
    	}
    	
    	return false; 
    }
    
    
    public double getEnderCD(UUID uuid){
    	double cd=0;
    	if (cooldowns.containsKey(uuid)){
    		cd = (config.getDouble("Enderpearl-Cooldown-Time")*1000 - ( System.currentTimeMillis() - cooldowns.get(uuid)))/1000;
    		if(cd < 0){
    			cd=0;
    		}
    	}
    	return cd;
    }
    
    public String getPlaceholderEnderPearlCDReady(){
    	return config.getString("PlaceholderEnderPearlCDReady");
    }
    
    /*

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {
    	if(event.isNewChunk() && event.getWorld().getName().equals("Magano")){
    	
    		World  w = event.getWorld();
    		Chunk c = event.getChunk();
    		for(int x = 0; x < 16; x++)
    		{
    		     for(int z = 0; z < 16; z++)
    		     {
    		          w.setBiome(c.getX()*16 + x, c.getZ()*16 +z, Biome.SKY);
    		     }
    		}
    		
    		//w.getEnvironment().s
    		//event.getChunk().
    		//System.out.print(event.getChunk().getX()+"-"+event.getChunk().getZ());
    		
    		//WorldCreator wc = new WorldCreator("world");
   
    	}
    }
    */
    
}
