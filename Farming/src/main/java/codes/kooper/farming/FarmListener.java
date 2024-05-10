package codes.kooper.farming;

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
import java.util.Set;

public class FarmListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Get the bounds of the stage
        BlockifyPosition pos1 = new BlockifyPosition(0, 0, 0);
        BlockifyPosition pos2 = new BlockifyPosition(10, 10, 10);

        // Create the audience by adding the player to a set
        Set<Player> players = new HashSet<>();
        players.add(player);

        // Create the stage
        Stage stage = new Stage(player.getUniqueId() + "-farm", Bukkit.getWorld("world"), pos1, pos2, new Audience(players, true));
        Blockify.getInstance().getStageManager().createStage(stage);

        // Set the amount of chunks sent to the audience per tick
        stage.setChunksPerTick(3); // Default is 1

        // Create a pattern with the blocks and their percentages (sum of all percentages must be 1)
        HashMap<BlockData, Double> blocks = new HashMap<>();
        blocks.put(BlockUtils.setAge(Material.WHEAT.createBlockData(), 7), 1.0);

        // Create the view with the pattern and add it to the stage
        View view = new View("farm", stage, new Pattern(blocks), true);
        stage.addView(view);

        // Add & send blocks asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Farming.getInstance(), () -> {
            // Add the blocks to the stage
            view.addBlocks(BlockUtils.getBlocksBetween(pos1, pos2));

            // Send the blocks to the audience (players) (All blocks will be sent)
            // Use stage.sendBlocksToAudience(positions) to send only certain blocks (Shown below)
            stage.sendBlocksToAudience();
        });
    }

    @EventHandler
    public void onBlockifyBlockBreak(BlockifyBreakEvent event) {
        if (!event.getView().getName().equalsIgnoreCase("farm")) return;

        // loop blocks around broken block
        Bukkit.getScheduler().runTaskAsynchronously(Farming.getInstance(), () -> {
            // Set blocks around to air for cool effect
            HashSet<BlockifyPosition> positions = new HashSet<>();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockifyPosition pos = new BlockifyPosition(event.getPosition().blockX() + x, event.getPosition().blockY() + y, event.getPosition().blockZ() + z);
                        event.getView().setBlock(pos, Material.AIR.createBlockData());
                        positions.add(pos);
                        event.getPlayer().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, pos.toLocation(event.getPlayer().getWorld()), 10, 0.5, 0.5, 0.5, 0.1, Material.WHEAT.createBlockData());
                    }
                }
            }

            // Send only the blocks around the broken block to the audience
            event.getStage().refreshBlocksToAudience(positions);
        });
    }

}
