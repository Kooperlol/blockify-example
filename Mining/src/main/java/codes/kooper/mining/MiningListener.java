package codes.kooper.mining;

import codes.kooper.blockify.Blockify;
import codes.kooper.blockify.events.BlockifyBreakEvent;
import codes.kooper.blockify.models.Audience;
import codes.kooper.blockify.models.Pattern;
import codes.kooper.blockify.models.Stage;
import codes.kooper.blockify.models.View;
import codes.kooper.blockify.types.BlockifyPosition;
import codes.kooper.blockify.utils.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.HashSet;

public class MiningListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Get the bounds of the stage
        BlockifyPosition pos1 = new BlockifyPosition(0, 0, 0);
        BlockifyPosition pos2 = new BlockifyPosition(10, 10, 10);

        // Create the audience by adding the player to a set
        HashSet<Player> players = new HashSet<>();
        players.add(player);

        // Create the stage
        Stage stage = new Stage(player.getUniqueId() + "-mine", Bukkit.getWorld("world"), pos1, pos2, new Audience(players, true));
        Blockify.getInstance().getStageManager().createStage(stage);

        // Set the amount of chunks sent to the audience per tick
        stage.setChunksPerTick(3); // Default is 1

        // Create a pattern with the blocks and their percentages (sum of all percentages must be 1)
        HashMap<BlockData, Double> blocks = new HashMap<>();
        blocks.put(Material.STONE.createBlockData(), 0.3);
        blocks.put(Material.COAL_ORE.createBlockData(), 0.2);
        blocks.put(Material.IRON_ORE.createBlockData(), 0.2);
        blocks.put(Material.GOLD_ORE.createBlockData(), 0.1);
        blocks.put(Material.DIAMOND_ORE.createBlockData(), 0.1);
        blocks.put(Material.EMERALD_ORE.createBlockData(), 0.1);

        // Create the view with the pattern and add it to the stage
        View view = new View("mine", stage, new Pattern(blocks), true);
        stage.addView(view);

        // Set custom mining speed (Optional)
        // Lower is faster, higher is slower (Default is 1f) (3f is 3 times slower, etc.)
        stage.getAudience().setMiningSpeed(player, 3f);

        // Hide players from each other (Optional)
        stage.getAudience().setArePlayersHidden(true);

        // Add & send blocks asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Mining.getInstance(), () -> {
            // Add the blocks to the stage
            view.addBlocks(BlockUtils.getBlocksBetween(pos1, pos2));

            // Send the blocks to the audience (players) (All blocks will be sent)
            // Use stage.sendBlocksToAudience(positions) to send only certain blocks (Shown below)
            stage.sendBlocksToAudience();
        });
    }

    @EventHandler
    public void onBlockifyBlockBreak(BlockifyBreakEvent event) {
        if (!event.getView().getName().equalsIgnoreCase("mine")) return;

        // Jackhammer
        Bukkit.getScheduler().runTaskAsynchronously(Mining.getInstance(), () -> {
            HashSet<BlockifyPosition> positions = new HashSet<>();

            // loop blocks on same y-coordinate between stage positions (pos1 and pos2)
            for (int x = event.getStage().getMinPosition().getX(); x <= event.getStage().getMaxPosition().getX(); x++) {
                for (int z = event.getStage().getMinPosition().getZ(); z <= event.getStage().getMaxPosition().getZ(); z++) {
                    positions.add(new BlockifyPosition(x, event.getPosition().blockY(), z));
                    event.getView().setBlock(new BlockifyPosition(x, event.getPosition().blockY(), z), Material.AIR.createBlockData());
                }
            }

            // Send only the blocks around the broken block to the audience
            event.getStage().refreshBlocksToAudience(positions);
        });
    }


}
