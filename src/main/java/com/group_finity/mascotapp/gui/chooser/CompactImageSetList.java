package com.group_finity.mascotapp.gui.chooser;

import com.group_finity.mascotapp.gui.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.Component;

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

            if (isSelected) {
                component.setBackground(Theme.SELECTION_HIGHLIGHT);
                component.setBorder(BorderFactory.createLineBorder(Theme.SELECTION_BORDER));
            } else if (index % 2 == 0) {
                component.setBackground(Theme.LIST_COLOR_DARK);
                component.setBorder(null);
            } else {
                component.setBackground(Theme.LIST_COLOR_LIGHT);
                component.setBorder(null);
            }

            return component;
        }
    }

}
