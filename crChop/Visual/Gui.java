package org.crChop.Visual;

import org.crChop.Enums.Tree;
import org.crChop.Tasks.*;
import org.crChop.crChop;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Equipment;
import org.powerbot.script.rt4.GameObject;
import org.powerbot.script.rt4.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Dakota on 9/9/2015.
 */
public class Gui extends JFrame {

    public boolean started = false;

    final JButton btnStart;
    final JButton btnCancel;
    final JComboBox<String> cboMethod;
    final JComboBox<Tree> cboTrees;
    final JCheckBox chkScreenshot;
    int axeId;
    String method;
    Tree tree;

    public Gui(final ClientContext ctx) {
        btnStart = new JButton("Start Script");
        btnCancel = new JButton("Cancel");
        cboMethod = new JComboBox<>(new String[]{"Drop"});
        cboTrees = new JComboBox<>(Tree.values());
        chkScreenshot = new JCheckBox("Save Screenshot");

        this.setLocationRelativeTo(Frame.getFrames()[0]);
        this.setTitle("crChop Gui");

        this.add(cboTrees);
        this.add(cboMethod);
        this.add(chkScreenshot);
        this.add(btnStart);
        this.add(btnCancel);

        this.setLayout(new FlowLayout());
        this.pack();

        if (!ctx.objects.select().name("Bank booth", "Grand Exchange booth").isEmpty()) {
            for (GameObject g : ctx.objects.nearest()) {
                HashSet<String> set = new HashSet<>();
                if (!set.contains("Bank : " + g.name())) {
                    cboMethod.addItem("Bank : " + g.name() + " : " + (int) g.tile().distanceTo(ctx.players.local()));
                    set.add("Bank : " + g.name());
                }
            }
        }

        cboTrees.setSelectedIndex(0);

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // GET AXE ID
                for (Item i : ctx.inventory.items()) {
                    if (i.name().toLowerCase().contains("axe")) {
                        System.out.println(i.name() + "(" + i.id() + ")");
                        axeId = i.id();
                    }
                }

                method = cboMethod.getSelectedItem().toString();
                tree = Tree.valueOf(cboTrees.getSelectedItem().toString());

                System.out.println(axeId);
                System.out.println(method);
                System.out.println(tree);

                if (cboMethod.getSelectedItem().toString().toLowerCase().contains("bank")) {
                    crChop.taskList.addAll(Arrays.asList(new Banking(ctx, method, axeId), new Run(ctx), new Inventory(ctx), new Chop(ctx, tree, axeId), new Antiban(ctx)));
                } else if (cboMethod.getSelectedItem().toString().toLowerCase().contains("drop")) {
                    crChop.taskList.addAll(Arrays.asList(new Drop(ctx), new Run(ctx), new Inventory(ctx), new Chop(ctx, tree, axeId), new Antiban(ctx)));
                }

                started = true;
                dispose();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ctx.controller.stop();
                dispose();
            }
        });
    }

    public String getMethod() {
        return cboMethod.getSelectedItem().toString();
    }

    public Tree getTree() {
        return Tree.valueOf(cboTrees.getSelectedItem().toString());
    }

    public boolean saveScreenshot() {
        return chkScreenshot.isSelected();
    }
}

