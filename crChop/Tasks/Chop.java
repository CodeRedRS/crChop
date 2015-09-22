package org.crChop.Tasks;

import org.crChop.Enums.Tree;
import org.crChop.Task;
import org.crChop.Visual.Paint;
import org.powerbot.script.Area;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Equipment;
import org.powerbot.script.rt4.GameObject;

import java.util.concurrent.Callable;

/**
 * Created by Dakota on 9/7/2015.
 */
public class Chop extends Task<ClientContext> {
    private Tree tree;
    private int axeId;

    public Chop(ClientContext ctx, Tree tree, int axeId) {
        super(ctx);
        this.tree = tree;
        this.axeId = axeId;
    }

    @Override
    public boolean activate() {
        return ctx.inventory.select().count() < 28
                && !ctx.objects.select().name(tree.getName()).isEmpty()
                && !ctx.players.local().inCombat() //TODO: Running from combat to bank temporary fix
                && (ctx.players.local().animation() == -1 && !ctx.players.local().inMotion())
                && (ctx.inventory.id(axeId).count() == 1 || ctx.equipment.itemAt(Equipment.Slot.MAIN_HAND) != null);
    }

    @Override
    public void execute() {
        GameObject tree = ctx.objects.nearest().poll();
        Area treeArea = new Area(new Tile(tree.tile().x() - 1, tree.tile().y() - 1), new Tile(tree.tile().x() + 1, tree.tile().y() + 1));

        if (ctx.bank.opened()) {
            ctx.movement.step(tree);
        }

        if (!tree.inViewport()) {
            ctx.camera.turnTo(tree);
            Paint.status = "Going to " + tree.name();
            if (!tree.inViewport()) {
                ctx.camera.pitch(Random.nextInt(0, 15));
                if (ctx.movement.step(tree)) {
                    ctx.camera.turnTo(tree);
                    if (ctx.players.local().inMotion()) {
                        Condition.wait(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return !ctx.players.local().inMotion() && (ctx.movement.destination().distanceTo(ctx.players.local()) < 10 || ctx.players.local().tile().distanceTo(tree) < 10);
                            }
                        }, 1000, 10);
                    }
                }
            }
        }

        if (tree.interact(true, "Chop down")) {
            Paint.status = "Chopping " + tree.name();
            if (ctx.camera.pitch() <= 15) {
                ctx.camera.pitch(Random.nextInt(25, 75));
            }
            Condition.wait(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return !ctx.players.local().inMotion() && ctx.players.local().animation() != -1;
                }
            }, 300, 10);
        }
    }
}
