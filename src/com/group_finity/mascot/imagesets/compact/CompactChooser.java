package com.group_finity.mascot.imagesets.compact;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.imagesets.ImageSetUI;
import com.group_finity.mascot.imagesets.ImageSetUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;


public class CompactChooser extends javax.swing.JDialog implements ImageSetUI {

    private JList<CompactImageSetPreview> imageSetJlist;

    // set to true by the "cancel" button
    private boolean cancelSelection = false;

    public CompactChooser(JFrame parent) {
        super(parent,true);
        createGui();
    }

    @Override
    public ArrayList<String> getSelections() {
        setVisible(true);

        if (cancelSelection){
            return null;
        }

        return imageSetJlist.getSelectedValuesList().stream()
                .map(Objects::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    /** GUI entry point */
    private void createGui() {
        if (this.isVisible()) {
            getOwner().toFront();
            return;
        }
        //Set up data and selections
        addDataToUI();

        //Set up the content pane.
        addContentToPane(getContentPane());

        this.setResizable(false);

        pack();
        setLocationRelativeTo(null);
        toFront();
    }

    private void addDataToUI() {
        DefaultListModel<CompactImageSetPreview> listModel = new DefaultListModel<>();

        String[] allImageSets = ImageSetUtils.getAllImageSets();

        if (allImageSets.length == 0) {
            imageSetJlist = new CompactImageSetList(listModel);
            return;
        }

        ArrayList<String> selected = ImageSetUtils.getImageSetsFromSettings();

        ArrayList<CompactImageSetPreview> data = new ArrayList<>(allImageSets.length);
        for (String imgSet : allImageSets) {
            data.add(new CompactImageSetPreview(imgSet));
        }
        listModel.addAll(data);

        imageSetJlist = new CompactImageSetList(listModel);

        //yes, i know this is horribly inefficient, im just hoping it wont be called enough to matter
        var selectedIndices = new ArrayList<Integer>();

        if (selected != null){
            for (String str : selected) {
                for (int j = 0; j < allImageSets.length; j++) {
                    if (allImageSets[j].equals(str)) {
                        selectedIndices.add(j);
                    }
                }
            }
        }

        // https://stackoverflow.com/questions/960431/
        imageSetJlist.setSelectedIndices(selectedIndices.stream().mapToInt(i -> i).toArray());

    }

    private void addContentToPane(Container pane) {
        var gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints constraints = new GridBagConstraints();

        //-------scroll view-------//
        var scPane = new JScrollPane(imageSetJlist);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.weightx = 2;
        constraints.gridy = 0;
        constraints.insets = new Insets(9,9,9,9);

        scPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scPane.setPreferredSize(new Dimension(400,400));

        pane.add(scPane,constraints);

        //--------buttons--------//
        var buttonCancel = new JButton(Main.getInstance().getLanguageBundle().getString("Cancel"));
        buttonCancel.addActionListener(e -> {
            cancelSelection = true;
            this.dispose();
        });

        var buttonOK = new JButton(Main.getInstance().getLanguageBundle().getString("UseSelected"));
        buttonOK.addActionListener(e -> this.dispose());

        this.getRootPane().setDefaultButton(buttonOK);

        constraints = new GridBagConstraints();//reset
        var btnsPanel = new JPanel(gbl);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridy = 1;
        constraints.insets = new Insets(0,9,9,9);
        //constraints.gridx = 1;
        btnsPanel.add(buttonCancel);
        btnsPanel.add(buttonOK);

        pane.add(btnsPanel,constraints);
    }


}
