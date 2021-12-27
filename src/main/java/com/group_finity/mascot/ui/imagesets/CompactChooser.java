package com.group_finity.mascot.ui.imagesets;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Tr;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CompactChooser extends JFrame {

    private JList<CompactImageSetPreview> imageSetJlist;

    Consumer<ArrayList<String>> onSelection;

    public CompactChooser(Consumer<ArrayList<String>> onSelection) {
        this.onSelection = onSelection;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * GUI entry point
     */
    public void createGui() {
        SwingUtilities.invokeLater(() -> {
            //Set up data and selections
            addDataToUI();

            //Set up the content pane.
            addContentToPane(getContentPane());

            this.setResizable(false);

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            toFront();
        });
    }

    private ArrayList<String> getSelections() {
        return imageSetJlist.getSelectedValuesList().stream()
                .map(Objects::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void addDataToUI() {
        DefaultListModel<CompactImageSetPreview> listModel = new DefaultListModel<>();

        String[] allImageSets;
        try {
            allImageSets = Main.getInstance().getProgramFolder().getImageSetNames().toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
            Main.showError("Unable to load imageSets");
            this.dispose();
            return;
        }

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

        for (String str : selected) {
            for (int j = 0; j < allImageSets.length; j++) {
                if (allImageSets[j].equals(str)) {
                    selectedIndices.add(j);
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
        constraints.insets = new Insets(9, 9, 9, 9);

        scPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scPane.setPreferredSize(new Dimension(400, 400));

        pane.add(scPane, constraints);

        //--------buttons--------//
        var buttonCancel = new JButton(Tr.tr("Cancel"));
        buttonCancel.addActionListener(e -> this.dispose());

        var buttonOK = new JButton(Tr.tr("UseSelected"));
        buttonOK.addActionListener(e -> {
            this.dispose();
            onSelection.accept(getSelections());
        });

        this.getRootPane().setDefaultButton(buttonOK);

        constraints = new GridBagConstraints();//reset
        var btnsPanel = new JPanel(gbl);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 9, 9, 9);
        //constraints.gridx = 1;
        btnsPanel.add(buttonCancel);
        btnsPanel.add(buttonOK);

        pane.add(btnsPanel, constraints);
    }

}
