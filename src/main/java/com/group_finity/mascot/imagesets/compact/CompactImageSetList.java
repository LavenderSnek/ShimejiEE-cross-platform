package com.group_finity.mascot.imagesets.compact;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

public class CompactImageSetList extends JList<CompactImageSetPreview> {

    CompactImageSetList(DefaultListModel<CompactImageSetPreview> defaultListModel) {
        super(defaultListModel);

        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setLayoutOrientation(JList.VERTICAL);
        this.setCellRenderer(new ImageSetRenderer());
    }

    private static class ImageSetRenderer implements ListCellRenderer<CompactImageSetPreview> {

        @Override
        public Component getListCellRendererComponent(JList<? extends CompactImageSetPreview> list, CompactImageSetPreview value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            var component = value.getPanel();

            if (index % 2 == 0) {
                component.setBackground(new Color(244, 245, 245));
            } else {
                component.setBackground(Color.white);
            }

            if (isSelected) {
                component.setBackground(SystemColor.textHighlight);
                component.setBorder(BorderFactory.createLineBorder(SystemColor.textHighlight.darker()));
            }

            return component;
        }
    }

}
