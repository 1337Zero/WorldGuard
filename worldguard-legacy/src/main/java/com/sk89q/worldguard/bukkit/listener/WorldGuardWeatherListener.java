/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldGuardWeatherListener implements Listener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin The plugin instance
     */
    public WorldGuardWeatherListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(event.getWorld()));

        if (event.toWeatherState()) {
            if (wcfg.disableWeather) {
                event.setCancelled(true);
            }
        } else {
            if (!wcfg.disableWeather && wcfg.alwaysRaining) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(event.getWorld()));

        if (event.toThunderState()) {
            if (wcfg.disableThunder) {
                event.setCancelled(true);
            }
        } else {
            if (!wcfg.disableWeather && wcfg.alwaysThundering) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLightningStrike(LightningStrikeEvent event) {
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(event.getWorld()));

        if (wcfg.disallowedLightningBlocks.size() > 0) {
            Material targetId = event.getLightning().getLocation().getBlock().getType();
            if (wcfg.disallowedLightningBlocks.contains(BukkitAdapter.asBlockType(targetId).getId())) {
                event.setCancelled(true);
            }
        }

        Location loc = event.getLightning().getLocation();
        if (wcfg.useRegions) {
            if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(loc), (RegionAssociable) null, Flags.LIGHTNING))) {
                event.setCancelled(true);
            }
        }
    }
}
