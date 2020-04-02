package plugin.quest.witchs_house;

import org.crandor.cache.def.impl.ItemDefinition;
import org.crandor.cache.def.impl.ObjectDefinition;
import org.crandor.game.content.global.action.DoorActionHandler;
import org.crandor.game.content.global.action.PickupHandler;
import org.crandor.game.content.skill.Skills;
import org.crandor.game.interaction.NodeUsageEvent;
import org.crandor.game.interaction.Option;
import org.crandor.game.interaction.OptionHandler;
import org.crandor.game.interaction.UseWithHandler;
import org.crandor.game.node.Node;
import org.crandor.game.node.entity.combat.ImpactHandler;
import org.crandor.game.node.entity.npc.NPC;
import org.crandor.game.node.entity.player.Player;
import org.crandor.game.node.entity.player.link.quest.Quest;
import org.crandor.game.node.item.GroundItem;
import org.crandor.game.node.item.GroundItemManager;
import org.crandor.game.node.item.Item;
import org.crandor.game.node.object.GameObject;
import org.crandor.game.world.map.Location;
import org.crandor.plugin.InitializablePlugin;
import org.crandor.plugin.Plugin;
import org.crandor.plugin.PluginManager;
import org.crandor.tools.RandomFunction;

/**
 * Created for 2009Scape
 * User: Ethan Kyle Millard
 * Date: March 15, 2020
 * Time: 9:54 AM
 */
@InitializablePlugin
public class WitchsHousePlugin extends OptionHandler {

    private static final Item LEATHER_GLOVES = new Item(1059);
    public static final Item KEY = new Item(2411);
    public static final Item DOOR_KEY = new Item(2409);
    private static final Item MAGNET = new Item(2410);
    public static final Item BALL = new Item(2407);
    private static final Item CHEESE = new Item(1985);

    @Override
    public boolean handle(Player player, Node node, String option) {
        boolean killedExperiment = player.getAttribute("killedExperiment", false);
        boolean experimentAlive = player.getAttribute("experimentSpawned", false);
        boolean readBook = player.getAttribute("readWitchsBook", false);
        boolean magnetAttatched = player.getAttribute("attached_magnet", false);
        WitchsExperimentNPC experiment = new WitchsExperimentNPC(WitchsExperimentNPC.ExperimentType.FIRST, Location.create(2935, 3462, 0));
        experiment.setPlayer(player);
        experiment.setRespawn(false);
        final Quest quest = player.getQuestRepository().getQuest("Witch's House");
        final GroundItem ball = GroundItemManager.get(2407, new Location(2935, 3460, 0), null);
        final int id = node instanceof Item ? ((Item) node).getId() : node instanceof GameObject ? ((GameObject) node).getId() : node instanceof NPC ? ((NPC) node).getId() : node.getId();
        switch (id) {
            case 2407:
                player.debug("Killed experiment " + player.getAttribute("killedExperiment", false));
                if (killedExperiment) {
                    if (player.getInventory().containsItem(BALL)) {
                        player.sendMessage("You already have the ball.");
                        return true;
                    }
                    PickupHandler.take(player, ball);
                    player.debug("Using default item handler, option: " + new Option("take", 0));
                    return true;
                }
                if (experimentAlive) {
                    int[] skillsToDecrease = {Skills.DEFENCE, Skills.ATTACK, Skills.STRENGTH, Skills.RANGE, Skills.MAGIC};
                    for (int i = 0; i < skillsToDecrease.length; i++) {
                        player.getSkills().setLevel(i, player.getSkills().getStaticLevel(i) > 5 ? player.getSkills().getStaticLevel(i) - 5 : 1);
                    }
                    player.getPacketDispatch().sendMessage("<col=ff0000>The experiment glares at you, and you feel yourself weaken.</col>");
                    ((NPC) experiment).
                            attack(player);
                    return true;
                }
                experiment = new WitchsExperimentNPC(WitchsExperimentNPC.ExperimentType.FIRST, Location.create(2935, 3462, 0));
                experiment.init();
                player.setAttribute("/save:killedExperiment", false);
                player.setAttribute("experimentSpawned", true);
                break;
            case 897:
            case 898:
            case 899:
            case 900:
                player.debug("Option is: " + option);
                if (option.equals("attack")) {
                    player.getProperties().getCombatPulse().attack(node);
                }
                break;
            case 24692:
                int[] items = {1733, 1059, 1061, 1965, 1734};
                for (int item : items) {
                    if (!player.getInventory().containsItem(new Item(item))) {
                        switch (item) {
                            case 1733:
                                player.getInventory().add(new Item(item));
                                player.sendMessage("You find a sewing needle in the bottom of one of the boxes!");
                                return true;
                            case 1734:
                                player.getInventory().add(new Item(item));
                                player.sendMessage("You find some sewing thread in the bottom of one of the boxes!");
                                return true;
                            case 1059:
                                player.getInventory().add(new Item(item));
                                player.sendMessage("You find a pair of leather gloves in the bottom of one of the boxes!");
                                return true;
                            case 1061:
                                player.getInventory().add(new Item(item));
                                player.sendMessage("You find a pair of leather boots in the bottom of one of the boxes!");
                                return true;
                            case 1965:
                                player.getInventory().add(new Item(item));
                                player.sendMessage("You find an old cabbage in the bottom of one of the boxes!");
                                return true;

                        }
                    }
                }
                player.sendMessage("You find nothing interesting in the boxes.");
                break;
            case 2869:
                if (player.getInventory().addIfDoesntHave(MAGNET)) {
                    player.getDialogueInterpreter().sendDialogue("You find a magnet in the cupboard.");
                } else {
                    player.sendMessage("You search the cupboard but find nothing interesting.");
                }
                break;
            case 2867:
                if (player.getInventory().addIfDoesntHave(DOOR_KEY)) {
                    player.getDialogueInterpreter().sendDialogue("You find a key hidden under the flower pot.");
                } else {
                    player.sendMessage("You search under the flower pot and find nothing.");
                }
                break;
            case 2861:
                if (quest.isCompleted(player)) {
                    player.sendMessage("The lock has seemed to changed since the last time you visited.");
                    break;
                }
                if (player.getInventory().containsItem(DOOR_KEY) || player.getLocation().getX() >= 2901) {
                    DoorActionHandler.handleAutowalkDoor(player, (GameObject) node);
                } else {
                    player.sendMessage("The door is locked.");
                }
                break;
            case 2862:
                if (magnetAttatched || player.getLocation().getY() < 3466) {
                    DoorActionHandler.handleAutowalkDoor(player, (GameObject) node);
                    player.removeAttribute("attached_magnet");
                } else {
                    player.getDialogueInterpreter().sendDialogue("Strange... I can't see any kind of lock or handle to open this door.");
                }
                break;
            case 2865:
            case 2866:
                if (player.getEquipment().containsItem(LEATHER_GLOVES)) {
                    DoorActionHandler.handleAutowalkDoor(player, (GameObject) node);
                } else {
                    player.getImpactHandler().manualHit(player, RandomFunction.random(2, 3), ImpactHandler.HitsplatType.NORMAL);
                    player.getDialogueInterpreter().sendDialogue("As your bare hands touch the gate you feel a shock.");
                }
                break;
            case 24721:
                player.sendMessage("You decide to not attract the attention of the witch by playing the piano.");
                break;
            case 2863:
                if (player.getLocation().getX() >= 2934) {
                    DoorActionHandler.handleAutowalkDoor(player, (GameObject) node);
                    return true;
                }
                if (player.getInventory().containsItem(KEY)) {
                    DoorActionHandler.handleAutowalkDoor(player, (GameObject) node);
                    if (killedExperiment || experimentAlive) {
                        return true;
                    }
                    experiment = new WitchsExperimentNPC(WitchsExperimentNPC.ExperimentType.FIRST, Location.create(2935, 3462, 0));
                    experiment.init();
                    player.setAttribute("/save:killedExperiment", false);
                    player.setAttribute("experimentSpawned", true);
                } else {
                    player.sendMessage("The door is locked.");
                }
                break;
            case 2864:
                player.debug(readBook + "");
                if (readBook && player.getInventory().addIfDoesntHave(KEY)) {
                    player.getDialogueInterpreter().sendDialogue("You search for the secret compartment mentioned in the diary.", "Inside it you find a small key. You take the key.");
                } else {
                    player.sendMessage("You search the fountain but find nothing.");
                }
                break;
            case 24724:
                player.sendMessage("The gramophone doesn't have a record on it.");
                break;
            case 24672:
                player.teleport(Location.create(2906, 3472, 1));
                break;
            case 24673:
                player.teleport(Location.create(2906, 3468, 0));
                break;
            case 2408:
                player.getDialogueInterpreter().open(4501993, node);
                player.setAttribute("/save:readWitchsBook", true);
                break;
        }
        return true;
    }

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
        PluginManager.definePlugin(new WitchsHouseUseWithHandler());
        PluginManager.definePlugin(new MouseNPC());
        ObjectDefinition.forId(2867).getConfigurations().put("option:look-under", this);
        ObjectDefinition.forId(2861).getConfigurations().put("option:open", this);
        ObjectDefinition.forId(2865).getConfigurations().put("option:open", this);
        ObjectDefinition.forId(2866).getConfigurations().put("option:open", this);
        ObjectDefinition.forId(2862).getConfigurations().put("option:open", this);
        ObjectDefinition.forId(24724).getConfigurations().put("option:wind-up", this);
        ObjectDefinition.forId(24673).getConfigurations().put("option:walk-down", this);
        ObjectDefinition.forId(24672).getConfigurations().put("option:walk-up", this);
        ObjectDefinition.forId(24721).getConfigurations().put("option:play", this);
        ObjectDefinition.forId(24692).getConfigurations().put("option:search", this);
        ObjectDefinition.forId(2869).getConfigurations().put("option:search", this);
        ObjectDefinition.forId(2863).getConfigurations().put("option:open", this);
        ObjectDefinition.forId(2864).getConfigurations().put("option:check", this);
        ItemDefinition.forId(2407).getConfigurations().put("option:take", this);
        ItemDefinition.forId(2408).getConfigurations().put("option:read", this);

        return this;
    }

    public static class WitchsHouseUseWithHandler extends UseWithHandler {

        private WitchsHouseUseWithHandler() {
            super(CHEESE.getId());
        }

        @Override
        public Plugin<Object> newInstance(Object arg) throws Throwable {
            addHandler(15518, OBJECT_TYPE, this);
            UseWithHandler.addHandler(901, UseWithHandler.NPC_TYPE, new UseWithHandler(2410) {
                @Override
                public Plugin<Object> newInstance(Object arg) throws Throwable {
                    addHandler(901, UseWithHandler.NPC_TYPE, this);
                    return this;
                }

                @Override
                public boolean handle(NodeUsageEvent event) {
                    Player player = event.getPlayer();
                    Item useditem = event.getUsedItem();
                    final NPC npc = (NPC) event.getUsedWith();
                    assert useditem != null;
                    assert npc != null;
                    if (useditem.getId() == MAGNET.getId() && npc.getId() == 901 && player.getAttribute("mouse_out") != null) {
                        player.getDialogueInterpreter().sendDialogue("You attach a magnet to the mouse's harness.");
                        player.removeAttribute("mouse_out");
                        if (player.getInventory().remove(MAGNET))
                            player.setAttribute("attached_magnet", true);

                    }
                    return true;
                }
            });
            return this;
        }

        @Override
        public boolean handle(NodeUsageEvent event) {
            Player player = event.getPlayer();
            Item useditem = event.getUsedItem();
            final GameObject object = (GameObject) event.getUsedWith();
            assert useditem != null;
            assert object != null;
            if (player.getAttribute("mouse_out") != null && useditem.getId() == CHEESE.getId() && object.getId() == 15518) {
                player.getDialogueInterpreter().sendDialogue("You can't do this right now.");
            }
            if (useditem.getId() == CHEESE.getId() && object.getId() == 15518 && player.getAttribute("mouse_out") == null) {
                if (player.getInventory().remove(CHEESE))
                    player.getDialogueInterpreter().sendDialogue("A mouse runs out of the hole.");
                    MouseNPC mouse = (MouseNPC) MouseNPC.create(901, Location.create(2903, 3466, 0));
                    mouse.setPlayer(player);
                    mouse.setRespawn(false);
                    mouse.setWalks(false);
                    mouse.init();
                    mouse.faceLocation(Location.create(2903, 3465, 0));
                    player.setAttribute("mouse_out", true);
            }
            return true;
        }
    }
}
