package me.rhys.anticheat.listeners;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.filter.LogFilter;
import me.rhys.anticheat.util.GUIUtils;
import me.rhys.anticheat.util.StaticUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Plugin.getInstance().getUserManager().add(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Plugin.getInstance().getUserManager().remove(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        final User user = Plugin.getInstance().getUserManager().getUser((Player) event.getWhoClicked());
        if (user == null) return;

        if (event.getClickedInventory() == null || event.getClickedInventory().getName() == null
                || (!event.getWhoClicked().hasPermission("monolith.command.gui")
                && !user.isAllPermissions())) return;

        final String guiName = ChatColor.stripColor(event.getClickedInventory().getName());
        final String anticheatName = StaticUtil.getAnticheatName();

        if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
            final String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (guiName.equalsIgnoreCase(anticheatName)) {
                event.setCancelled(true);

                if (itemName.equalsIgnoreCase("Checks")) {
                    event.getWhoClicked().closeInventory();

                    Inventory inventory = Bukkit.getServer().createInventory(null, 27,
                            ChatColor.RED + "Checks");

                    inventory.setItem(11, GUIUtils.generateItem(
                            new ItemStack(Material.DIAMOND_SWORD, 1), ChatColor.GREEN + "Combat",
                            Collections.singletonList(ChatColor.GRAY + "Click to manage all combat checks.")));

                    inventory.setItem(13, GUIUtils.generateItem(
                            new ItemStack(Material.FEATHER, 1), ChatColor.GREEN + "Movement",
                            Collections.singletonList(ChatColor.GRAY + "Click to manage all movement checks.")));

                    inventory.setItem(15, GUIUtils.generateItem(
                            new ItemStack(Material.ANVIL, 1), ChatColor.GREEN + "Other",
                            Collections.singletonList(ChatColor.GRAY + "Click to manage all other checks.")));

                    for (int slots = 0; slots < 27; slots++) {
                        if (inventory.getItem(slots) == null) inventory.setItem(slots,
                                GUIUtils.createSpacer((byte) 7));
                    }

                    event.getWhoClicked().openInventory(inventory);
                }

                return;
            }

            switch (guiName) {

                case "Combat":
                case "Movement":
                case "Other": {
                    event.setCancelled(true);

                    Check clickedCheck = Plugin.getInstance().getStaticCheckManager().getCheckList().stream()
                            .filter(check -> (check.getName() + check.getType()).equalsIgnoreCase(itemName))
                            .findAny().orElse(null);

                    switch (event.getClick()) {
                        case LEFT: {
                            if (clickedCheck != null) {

                                clickedCheck.setEnabled(!clickedCheck.isEnabled());

                                Plugin.getInstance().getUserManager().getUserMap().forEach((uuid, user1) ->
                                        user1.getCheckManager().getChecks().stream().filter(check ->
                                                (check.getName() + check.getType()).equalsIgnoreCase
                                                        ((clickedCheck.getName()
                                                                + clickedCheck.getType()))).forEach(check ->
                                                check.setEnabled(clickedCheck.isEnabled())));

                                this.showChecksUI(event, user.lastUIName);
                            }
                            break;
                        }

                        case RIGHT: {
                            if (clickedCheck != null) {

                                clickedCheck.setBan(!clickedCheck.isBan());

                                Plugin.getInstance().getUserManager().getUserMap().forEach((uuid, user1) ->
                                        user1.getCheckManager().getChecks().stream().filter(check ->
                                                (check.getName() + check.getType()).equalsIgnoreCase
                                                        ((clickedCheck.getName()
                                                                + clickedCheck.getType()))).forEach(check ->
                                                check.setBan(clickedCheck.isBan())));

                                this.showChecksUI(event, user.lastUIName);
                            }
                            break;
                        }

                        case MIDDLE: {
                            if (clickedCheck != null) {

                                clickedCheck.setEnabled(!clickedCheck.isEnabled());
                                clickedCheck.setBan(!clickedCheck.isBan());

                                Plugin.getInstance().getUserManager().getUserMap().forEach((uuid, user1) ->
                                        user1.getCheckManager().getChecks().stream().filter(check ->
                                                (check.getName() + check.getType()).equalsIgnoreCase
                                                        ((clickedCheck.getName()
                                                                + clickedCheck.getType()))).forEach(check -> {
                                            check.setEnabled(clickedCheck.isEnabled());
                                            check.setBan(clickedCheck.isBan());
                                        }));

                                this.showChecksUI(event, user.lastUIName);
                            }
                            break;
                        }
                    }

                    if (clickedCheck != null) {
                        Plugin.getInstance().getStaticCheckManager().saveChecks();
                    }
                    break;
                }

                case "Checks": {

                    switch (itemName) {
                        case "Combat":
                        case "Movement":
                        case "Other": {
                            user.lastUIName = itemName;
                            this.showChecksUI(event, itemName);
                            break;
                        }
                    }

                    break;
                }
            }
        }

        switch (guiName) {

            case "Combat":
            case "Movement":
            case "Other":
            case "Checks":
            case "Monolith": {
                event.setCancelled(true);
                break;
            }
        }
    }

    private void showChecksUI(InventoryClickEvent event, String itemName) {
        event.getWhoClicked().closeInventory();

        Inventory inventory = Bukkit.getServer().createInventory(null, 54,
                ChatColor.RED + itemName);

        AtomicInteger slot = new AtomicInteger();

        CheckType toSearch = CheckType.COMBAT;

        switch (itemName) {
            case "Movement": {
                toSearch = CheckType.MOVEMENT;
                break;
            }

            case "Other": {
                toSearch = CheckType.OTHER;
                break;
            }
        }

        CheckType finalToSearch = toSearch;
        Plugin.getInstance().getStaticCheckManager().getCheckList().stream().filter(check ->
                check.getCheckType() == finalToSearch).forEach(check -> {
            inventory.setItem(slot.getAndIncrement(),
                    GUIUtils.generateItem(new ItemStack((check.isEnabled() ? (check.isBan() ? Material.EMERALD_BLOCK
                                    : Material.GOLD_BLOCK) : Material.REDSTONE_BLOCK), 1),
                            (check.isEnabled() ? (check.isBan() ? ChatColor.GREEN
                                    : ChatColor.YELLOW) : ChatColor.RED)
                                    + check.getName() + check.getType(), Arrays.asList(
                                    ChatColor.GRAY + "Enabled: " + (check.isEnabled() ?
                                            ChatColor.GREEN : ChatColor.RED) + check.isEnabled(),
                                    ChatColor.GRAY + "Ban: "+ (check.isBan() ?
                                            ChatColor.GREEN : ChatColor.RED) + check.isBan()
                            )));
        });

        for (int slots = 0; slots < 54; slots++) {
            if (inventory.getItem(slots) == null) inventory.setItem(slots,
                    GUIUtils.createSpacer((byte) 7));
        }

        event.getWhoClicked().openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (LogFilter.LOG4J_RCE_PATTERN.matcher(event.getMessage()).matches()) {
            event.setMessage(event.getMessage().replace("$", ""));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (LogFilter.LOG4J_RCE_PATTERN.matcher(event.getMessage()).matches()) {
            event.setMessage(event.getMessage().replace("$", ""));
        }

        if (Plugin.getInstance().getConfigValues().isHider()
                && Plugin.getInstance().getConfigValues().isBlockGeneralCommands() && !event.getPlayer().isOp()) {

            final String message = event.getMessage();

            if (message.startsWith("help")
                    || message.startsWith("about")
                    || message.startsWith("plugins")
                    || message.startsWith("pl")) {
                event.setCancelled(true);
            }
        }
    }

    void process(Event event) {
        Plugin.getInstance().getExecutorService().execute(() -> {

            if (event instanceof PlayerChangedWorldEvent) {
                PlayerChangedWorldEvent playerChangedWorldEvent = (PlayerChangedWorldEvent) event;

                User user = Plugin.getInstance().getUserManager().getUser(playerChangedWorldEvent.getPlayer());

                if (user != null) {
                    user.getWorldChangeEvent().reset();
                }
            }

            if (event instanceof EntityDamageEvent) {
                EntityDamageEvent entityDamageEvent = (EntityDamageEvent) event;

                if (entityDamageEvent.getEntity() instanceof Player) {
                    User user = Plugin.getInstance().getUserManager().getUser(((Player)
                            entityDamageEvent.getEntity()).getPlayer());

                    if (user != null && entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.FALL) {
                        user.getConnectionProcessor().queue(() -> user.getActionProcessor()
                                .reduceVelocitySpeed(.2, false));
                    }
                }
            }

            if (event instanceof PlayerTeleportEvent) {
                PlayerTeleportEvent playerTeleportEvent = (PlayerTeleportEvent) event;
                User user = Plugin.getInstance().getUserManager().getUser(playerTeleportEvent.getPlayer());;

                if (user != null && playerTeleportEvent.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                    user.getConnectionProcessor().queue(() ->
                            user.getActionProcessor().getEnderPearlTimer().reset());
                }
            }
        });
    }
}
