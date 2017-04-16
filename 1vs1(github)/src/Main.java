
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

	Location l = new Location(Bukkit.getWorld("world"), 8.5, 4, 8.5);
	private static GameState gs = GameState.LOBBY;
	int ct;
	int c = 30;
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		Countdown();
	}
	
	private void TeleportPlayers (Location middle, int radius) {
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
	
	private void FasterRespawn (Player p) {
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				((CraftPlayer)p).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
				p.teleport(l);
			}
		}, 1);
	}
	
	private void ResetGame() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.teleport(l);
		}
		gs = GameState.LOBBY;
		c = 30;
		Countdown();
	}
	
	private void Countdown() {
		ct = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (c == 0) {
					if (Bukkit.getOnlinePlayers().size() == 2) {
						getServer().getScheduler().cancelTask(ct);
						for (Player p : Bukkit.getOnlinePlayers()) {
							TeleportPlayers(l, 15);
							p.getInventory().clear();
							p.getInventory().setArmorContents(null);
							p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
							p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
							p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
							p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
							p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
						}
						Bukkit.broadcastMessage("§b[1vs1] §6Rundenstart jetzt!");
						gs = GameState.INGAME;
					} else {
						Bukkit.broadcastMessage("§b[1vs1] §cEs sind nicht genügend Spieler online, um das Spiel zu starten!");
						c = 30;
					}
				} if (c == 30 || c == 20 || c == 10 || c == 5 || c == 4 || c == 3 || c == 2 || c == 1) {
					Bukkit.broadcastMessage("§b[1vs1] §6Rundenstart §7in §6" + c + " Sekunde(n)§7!");
				}
				c--;	
			}
		}, 0L, 20L);
	}
	
	@EventHandler
	private void onEntityDamage (EntityDamageEvent e) {
		if (gs != GameState.INGAME) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	private void onPlayerDeath (PlayerDeathEvent e) {
		e.getDrops().clear();
		e.setDeathMessage("§b[1vs1] §6" + e.getEntity().getKiller().getName() + " §7hat §agewonnen§7!");
		Player p = e.getEntity().getPlayer();
		FasterRespawn(p);
		ResetGame();
	}
	
	@EventHandler
	private void onPlayerJoin (PlayerJoinEvent e) {
		Player p = e.getPlayer();
		p.teleport(l);
	}
	
	@EventHandler
	private void onPlayerQuit (PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (gs != GameState.LOBBY) {
			Bukkit.broadcastMessage("§b[1vs1] §6Niemand hat gewonnen :c!");
			ResetGame();
		}
		e.setQuitMessage("§b[1vs1] §c-" + p.getName());
	}
}