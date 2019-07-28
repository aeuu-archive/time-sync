package io.arct.timesync.commands

import io.arct.timesync.TimeSync
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class TSync(private val plugin: TimeSync) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("timesync.tsync")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You do not have permission to do this!"))
            return true
        }

        if (args.size != 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Invalid Syntax! Usage: &e/tsync <world>"))
            return true
        }

        if (!Bukkit.getWorlds().any { it.name == args[0] }) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Could not find world &f${args[0]}!"))
            return true
        }

        val world = Bukkit.getWorld(args[0])!!
        val config = plugin.config

        val settings: String = when {
            config.getBoolean("${world.name}.ignore") -> {
                "&eIgnored: &fTrue"
            }

            config.getString("${world.name}.sync") != null &&
            config.getString("${world.name}.sync")!! != "none" -> {
                "&eSynced With: &f${config.getString("${world.name}.sync")!!}"
            }

            else -> {
                "&eDay: &f${config.getInt("${world.name}.day")}\n&eNight: &f${config.getInt("${world.name}.night")}"
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Current Settings for world &f${world.name}:\n$settings"))

        return true
    }
}