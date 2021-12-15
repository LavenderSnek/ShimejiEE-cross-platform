package com.group_finity.mascot.ui.interactivewindows;

import com.group_finity.mascot.Main;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Kilkakon
 */
public class InteractiveWindowForm extends JDialog {

    ArrayList<String> listData = new ArrayList<String>();

    public InteractiveWindowForm(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);

        listData.addAll(Arrays.asList(Main.getInstance().getProperties().getProperty("InteractiveWindows", "").split("/")));
        jList1.setListData(listData.toArray(new String[0]));
    }

    public void display() {
        setTitle(Main.getInstance().getLanguageBundle().getString("InteractiveWindows"));
        btnAdd.setText(Main.getInstance().getLanguageBundle().getString("Add"));
        btnDone.setText(Main.getInstance().getLanguageBundle().getString("Done"));
        btnRemove.setText(Main.getInstance().getLanguageBundle().getString("Remove"));
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

        jList1 = new JList<>();
        btnAdd = new JButton();
        btnRemove = new JButton();
        btnDone = new JButton();
        JPanel jPanel1 = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        JPanel jPanel2 = new JPanel();

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Interactive Windows");

        jList1.setModel(new javax.swing.AbstractListModel<>() {
            String[] strings = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

            public int getSize() {
                return strings.length;
            }

            public String getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(jList1);

        getContentPane().add(jScrollPane1, BorderLayout.CENTER);

        jPanel2.setLayout(new GridLayout(1, 0));

        btnAdd.setText("Add");
        btnAdd.addActionListener(this::addItem);
        jPanel2.add(btnAdd);

        btnRemove.setText("Remove");
        btnRemove.addActionListener(this::removeItem);
        jPanel2.add(btnRemove);

        btnDone.setText("Done");
        btnDone.addActionListener(this::confirmSelection);
        jPanel2.add(btnDone);

        getContentPane().add(jPanel2, BorderLayout.PAGE_END);

        pack();
    }

    private void addItem(ActionEvent evt) {
        // add button
        String inputValue = JOptionPane.showInputDialog(
                rootPane,
                Main.getInstance().getLanguageBundle().getString("InteractiveWindowHintMessage"),
                Main.getInstance().getLanguageBundle().getString("AddInteractiveWindow"),
                JOptionPane.QUESTION_MESSAGE
                ).trim();

        if (!inputValue.isEmpty() && !inputValue.contains("/")) {
            listData.add(inputValue);
            jList1.setListData(listData.toArray(new String[0]));
        }
    }

    private void removeItem(ActionEvent evt) {
        // delete button
        if (jList1.getSelectedIndex() != -1) {
            listData.remove(jList1.getSelectedIndex());
            jList1.setListData(listData.toArray(new String[0]));
        }
    }

    private void confirmSelection(ActionEvent evt) {
        // done button
        try {
            StringBuilder serializedProperty = new StringBuilder();
            for (String s : listData) {
                serializedProperty.append(s);
            }
            Main.getInstance().getProperties().setProperty("InteractiveWindows", serializedProperty.toString());
        } catch (Exception ignored) {
            // Doesn't matter at all
        }
        dispose();
    }

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnDone;
    private JList<String> jList1;
}
