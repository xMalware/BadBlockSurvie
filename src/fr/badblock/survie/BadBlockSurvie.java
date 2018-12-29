package fr.badblock.survie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.sk89q.wepif.PermissionsProvider;

import fr.badblock.api.common.utils.permissions.PermissionsManager;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.events.api.PlayerLoadedEvent;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.utils.BukkitUtils;

public class BadBlockSurvie extends JavaPlugin implements Listener, PermissionsProvider
{

	private Map<String, String> teamId = new HashMap<>();

	public char generateForId(int id){
		int A = 'A';

		if(id > 26){
			A   = 'a';
			id -= 26;

			return (char) (A + id);
		} else {
			return (char) (A + id);
		}
	}

	int i2 = 0;
	
	@Override
	public void onEnable()
	{
		this.getServer().getPluginManager().registerEvents(this, this);
		GameAPI gameApi = GameAPI.getAPI();
		
		gameApi.formatChat(true, false, "freebuild");

		PermissionsManager.getManager().getGroups().stream().sorted((a, b) -> {
			return Integer.compare(b.getPower(), a.getPower());
		}).forEach(group -> {
			String id = generateForId(i2) + "";
			teamId.put(group.getName(), id);
			i2++;
		});

		Bukkit.getScheduler().runTaskTimer(this, new Runnable()
		{

			@SuppressWarnings({ "deprecation" })
			@Override
			public void run()
			{
				for (BadblockPlayer player : BukkitUtils.getAllPlayers())
				{

					player.removePotionEffect(PotionEffectType.SLOW_DIGGING);

					for (BadblockPlayer plo : BukkitUtils.getAllPlayers())
					{
						Scoreboard scoreboard = plo.getScoreboard();

						if (scoreboard == null)
						{
							continue;
						}

						String mainGroup = teamId.get(player.getMainGroup());
						Team team = scoreboard.getTeam(mainGroup);

						String prfx = player.getTabGroupPrefix().getAsLine(plo);

						if (team != null)
						{
							if (!team.getPrefix().equalsIgnoreCase(prfx))
							{
								team.unregister();
							}
						}

						if (team == null)
						{
							team = scoreboard.registerNewTeam(mainGroup);
							team.setPrefix(prfx);
						}

						if (!team.hasPlayer(player))
						{
							team.addPlayer(player);							
						}

						for (Team t : scoreboard.getTeams())
						{
							if (t.equals(team))
							{
								continue;
							}

							if (t.hasPlayer(player))
							{
								team.removePlayer(player);
							}
						}
					}
				}

			}

		}, 20, 20);

	}

	@EventHandler
	public void onLoaded(PlayerLoadedEvent event)
	{
		BadblockPlayer player = event.getPlayer();
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				for (BadblockPlayer plo : BukkitUtils.getAllPlayers())
				{
					Scoreboard scoreboard = plo.getScoreboard();

					if (scoreboard == null)
					{
						continue;
					}

					Team team = scoreboard.getTeam(player.getMainGroup());

					if (team == null)
					{
						team = scoreboard.registerNewTeam(player.getMainGroup());
						team.setPrefix(player.getTabGroupPrefix().getAsLine(plo));
					}

					team.addPlayer(player);

					for (Team t : scoreboard.getTeams())
					{
						if (t.equals(team))
						{
							continue;
						}

						if (t.hasPlayer(player))
						{
							team.removePlayer(player);
						}
					}
				}
			}
		});
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		event.setDeathMessage("");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		event.setJoinMessage("§f[§a+§f] " + event.getPlayer().getName());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage("§f[§c-§f] " + event.getPlayer().getName());
	}


	@Override
	public boolean hasPermission(String worldName, String name, String permission)
	{
		Player plo = Bukkit.getPlayer(name);
		if (plo != null) {
			return plo.hasPermission(permission);
		}
		return false;
	}

	@Override
	public boolean hasPermission(OfflinePlayer player, String permission)
	{
		Player plo = Bukkit.getPlayer(player.getUniqueId());
		if (plo != null) {
			return plo.hasPermission(permission);
		}
		return false;
	}

	@Override
	public boolean hasPermission(String worldName, OfflinePlayer player, String permission)
	{
		Player plo = Bukkit.getPlayer(player.getUniqueId());
		if (plo != null) {
			return plo.hasPermission(permission);
		}
		return false;
	}

	@Override
	public boolean inGroup(OfflinePlayer player, String group)
	{
		boolean in = false;
		if (player != null)
		{
			Player plo = Bukkit.getPlayer(player.getUniqueId());
			if (plo != null) {
				if ((plo instanceof BadblockPlayer))
				{
					BadblockPlayer pla = (BadblockPlayer)plo;
					in = (group.equalsIgnoreCase(pla.getMainGroup())) || ((pla.getAlternateGroups() != null) && (pla.getAlternateGroups().contains(group)));
				}
			}
		}
		return (in);
	}

	public String[] getGroups(OfflinePlayer player)
	{
		List<String> s = new ArrayList();
		if (player != null)
		{
			Player plo = Bukkit.getPlayer(player.getUniqueId());
			if (plo != null) {
				if ((plo instanceof BadblockPlayer))
				{
					BadblockPlayer pla = (BadblockPlayer)plo;
					if (pla.getMainGroup() != null) {
						s.add(pla.getMainGroup());
					}
					if (pla.getAlternateGroups() != null) {
						s.addAll(pla.getAlternateGroups());
					}
				}
			}
		}
		if (!s.isEmpty())
		{
			String[] stockArr = new String[s.size()];
			stockArr = (String[])s.toArray(stockArr);
			return stockArr;
		}

		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String[] getGroups(String arg0) {
		List<String> g = new ArrayList<>();
		for (String s : getGroups(Bukkit.getOfflinePlayer(arg0)))
		{
			g.add(s);
		}

		String[] stockArr = new String[g.size()];
		stockArr = g.toArray(stockArr);

		return stockArr;
	}

	@Override
	public boolean hasPermission(String arg0, String arg1) {
		return hasPermission(Bukkit.getOfflinePlayer(arg0), arg1);
	}

	@Override
	public boolean inGroup(String arg0, String arg1) {
		return inGroup(Bukkit.getOfflinePlayer(arg0), arg1);
	}
	
}