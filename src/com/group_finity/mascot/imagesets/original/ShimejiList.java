package com.group_finity.mascot.imagesets.original;

import javax.swing.*;
import java.awt.*;

/**
 * A JList that can be populated with ImageSetChooserPanel objects
 */
class ShimejiList extends JList<ImageSetChooserPanel> {

  public ShimejiList() {
    setSelectionModel(new DefaultListSelectionModel() {
      @Override
      public void setSelectionInterval(int index0, int index1) {
        if (isSelectedIndex(index0)) {
          super.removeSelectionInterval(index0, index1);
        } else {
          super.addSelectionInterval(index0, index1);
        }
      }
    });
    setCellRenderer(new CustomCellRenderer());
  }

  static class CustomCellRenderer implements ListCellRenderer<ImageSetChooserPanel> {

    @Override
    public Component getListCellRendererComponent(JList<? extends ImageSetChooserPanel> list, ImageSetChooserPanel value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value != null) {
        value.setForeground(Color.white);
        value.setBackground(isSelected ? SystemColor.controlHighlight : Color.white);
        value.setCheckbox(isSelected);
        return value;
      } else {
        return new JLabel("???");
      }
    }
  }
}