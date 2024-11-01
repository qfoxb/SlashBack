package com.qfoxb.slashback;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = "slashback", name = "SlashBack", version = "2.0", acceptableRemoteVersions = "*", serverSideOnly = true)
public class SlashBackMod {

    private static final Map<String, BlockPos> lastDeathPositions = new HashMap<>();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        PermissionAPI.registerNode("slashback.back", DefaultPermissionLevel.ALL, "Allows use of the /back command");
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new BackCommand());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            lastDeathPositions.put(player.getName(), player.getPosition());
        }
    }

    public static class BackCommand extends CommandBase {
        @Override
        public String getName() {
            return "back";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/back - Return to your last death position.";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            if (sender instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) sender;
                BlockPos deathPos = lastDeathPositions.get(player.getName());
                if (deathPos != null) {
                    player.setPositionAndUpdate(deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5);
                    player.sendMessage(new TextComponentString("Teleported to your last death position."));
                } else {
                    player.sendMessage(new TextComponentString("No recorded death position found."));
                }
            } else {
                sender.sendMessage(new TextComponentString("This command can only be used by players."));
            }
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0; // Allow all players to use the command
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            if (sender instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) sender;
                return PermissionAPI.hasPermission(player, "slashback.back");
            }
            return false;
        }
    }
}
