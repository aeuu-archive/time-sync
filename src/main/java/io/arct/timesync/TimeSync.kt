package io.arct.timesync

import io.arct.timesync.commands.TSync
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Bukkit
import org.bukkit.World

class TimeSync : JavaPlugin() {
    val timeDay: MutableMap<World, Int> = HashMap()
    val timeNight: MutableMap<World, Int> = HashMap()
    val speedDay: MutableMap<World, Int> = HashMap()
    val speedNight: MutableMap<World, Int> = HashMap()

    override fun onEnable() {
        saveDefaultConfig()

        commands()

        loadWorlds()
        hook()
    }

    override fun onDisable() {
        saveConfig()
    }

    private fun commands () {
        getCommand("tsync")!!.setExecutor(TSync(this))
    }

    private fun hook () {
        server.scheduler.scheduleSyncRepeatingTask(this, {
            for (world in Bukkit.getWorlds()) {
                if (!timeDay.contains(world) || !timeNight.contains(world))
                    continue

                if (world.time > 12000) {
                    timeNight[world] = (timeNight[world]!! % 12000) + 1

                    world.time += (240.0 / speedNight[world]!!).toLong()
                } else {
                    timeDay[world] = (timeDay[world]!! % 12000) + 1

                    world.time += (240.0 / speedNight[world]!!).toLong()
                }
            }
        }, 0, 1)
    }

    private fun loadWorlds () {
        val requireSync: MutableList<World> = mutableListOf()

        for (world in Bukkit.getWorlds()) {
            if (!config.contains(world.name)) {
                config.addDefault("${world.name}.day", 900)
                config.addDefault("${world.name}.night", 900)
                config.addDefault("${world.name}.sync", "none")
                config.options().copyDefaults(true)

                saveConfig()
            }

            if (config.contains("${world.name}.ignore") && !config.getBoolean("${world.name}.ignore"))
                continue

            if (!config.contains("${world.name}.sync")) {
                println("Invalid Time Configuration for World ${world.name}, ignoring...")
                continue
            }

            val sync = config.getString("${world.name}.sync")!!
            val day = config.getInt("${world.name}.day")
            val night = config.getInt("${world.name}.night")

            if (sync.toLowerCase() != "none") {
                requireSync.add(world)
                continue
            }

            if (day < 1 || night < 1) {
                println("Invalid Time Configuration for World ${world.name}, ignoring...")
                continue
            }

            timeDay[world] = 0
            timeNight[world] = 0

            speedDay[world] = day
            speedNight[world] = night
        }

        for (world in requireSync) {
            val sync = config.getString("${world.name}.sync")!!

            var day: Int? = null
            var night: Int? = null

            for (w in timeDay.keys)
                if (w.name.toLowerCase() == sync.toLowerCase()) {
                    day = speedDay[w]
                    night = speedNight[w]
                }

            if (day == null || night == null || day < 0 || night < 0) {
                println("Invalid Time Configuration for World ${world.name}, ignoring...")
                continue
            }

            timeDay[world] = 0
            timeNight[world] = 0
            speedDay[world] = day
            speedNight[world] = night
        }

        for (world in Bukkit.getWorlds())
            world.time = 0
    }
}