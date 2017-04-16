
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_11_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_11_R1.PacketPlayInClientCommand.EnumClientCommand;

public class Main extends JavaPlugin implements Listener {

	public static GameState gs = GameState.LOBBY;
	int ct;
	int c = 30;
	
	public void onEnable() {
		cd();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	/*
	 * Methods
	 */
	
	void tp (Location middle, int radius) {
		float degree = 360 / Bukkit.getOnlinePlayers().size();
		int i = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			middle.setYaw(360 - i * degree);
			p.teleport(middle);
			Vector v = p.getEyeLocation().getDirection();
			Vector vec = v.multiply(radius);
			p.teleport(p.getLocation().add(vec));
			i++;
		}
	}
	
	void respawn(Player p) {
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				((CraftPlayer)p).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
				Location l = new Location(Bukkit.getWorld("world"), 8.5, 4, 8.5);
				p.teleport(l);
			}
		}, 1);
	}
	
	void reset() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			Location l = new Location(Bukkit.getWorld("world"), 8.5, 4, 8.5);
			p.teleport(l);
		}
		gs = GameState.LOBBY;
		c = 30;
		cd();
	}
	
	void cd() {
		ct = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (c == 0) {
					if (Bukkit.getOnlinePlayers().size() == 2) {
						getServer().getScheduler().cancelTask(ct);
						gs = GameState.INGAME;
						Location l = new Location(Bukkit.getWorld("world"), 8.5, 4, 8.5);
						for (Player p : Bukkit.getOnlinePlayers()) {
							tp(l, 15);
							p.getInventory().clear();
							p.getInventory().setArmorContents(null);
							p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
							p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
							p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
							p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
							p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
						}
						Bukkit.broadcastMessage("§b[1vs1] §6Rundenstart jetzt!");
						return;
					}
					Bukkit.broadcastMessage("§b[1vs1] §cEs sind nicht genügend Spieler online, um das Spiel zu starten!");
					c = 30;
				} if (c == 30 || c == 20 || c == 10 || c == 5 || c == 4 || c == 3 || c == 2) {
					Bukkit.broadcastMessage("§b[1vs1] §6Rundenstart §7in §6" + c + " Sekunden§7!");
				} else if (c == 1) {
					Bukkit.broadcastMessage("§b[1vs1] §6Rundenstart §7in §61 Sekunde§7!");
				}
				c--;
			}
		}, 0L, 20L);
	}
	
	
	/*
	 * Events
	 */
	
	@EventHandler
	public void onEntityDamage (EntityDamageEvent e) {
		if (gs != GameState.INGAME) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDeath (PlayerDeathEvent e) {
		e.getDrops().clear();
		Player p = e.getEntity().getPlayer();
		respawn(p);
		e.setDeathMessage("§b[1vs1] §6" + e.getEntity().getKiller().getDisplayName() + " §7hat §agewonnen§7!");
		reset();
	}
	
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Location l = new Location(Bukkit.getWorld("world"), 8.5, 4, 8.5);
		p.teleport(l);
		/*
		 * Chat color
		 */
		if(!p.isOp()) {
			p.setDisplayName("§7" + p.getName() + "§r");
		} else {
			p.setDisplayName("§c[Operator] " + p.getName() + "§r");
		}
		e.setJoinMessage("§b[1vs1] §a+" + p.getDisplayName());
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (gs != GameState.LOBBY) {
			Bukkit.broadcastMessage("§b[1vs1] §6Niemand hat gewonnen :c!");
			reset();
		}
		e.setQuitMessage("§b[1vs1] §c-" + p.getDisplayName());
	}
}