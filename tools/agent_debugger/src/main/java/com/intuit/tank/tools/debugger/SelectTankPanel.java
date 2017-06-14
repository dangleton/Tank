package com.intuit.tank.tools.debugger;

/*
 * #%L
 * Intuit Tank Agent Debugger
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import com.intuit.tank.harness.data.HDTestPlan;
import com.intuit.tank.tools.debugger.ActionProducer.IconSize;

public class SelectTankPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private AgentDebuggerFrame parent;

    private JComboBox<String> cb;

    private JTextField userTF;

    private JPasswordField passTF;

    public SelectTankPanel(AgentDebuggerFrame parent, JComboBox<String> cb) {
        super(new GridLayout(3, 2));
        this.cb = cb;
        // this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.parent = parent;
        add(new JLabel("URL: "));
        add(cb);
        add(new JLabel("User: "));
        userTF = new JTextField();
        add(userTF);

        add(new JLabel("Pass: "));
        passTF = new JPasswordField();
        add(passTF);
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public AgentDebuggerFrame getParent() {
        return parent;
    }

    public JComboBox<String> getCb() {
        return cb;
    }

    public JTextField getUserTF() {
        return userTF;
    }

    public JPasswordField getPassTF() {
        return passTF;
    }

}
