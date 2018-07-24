package com.cframo;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.event.*;
import java.io.FileWriter;

public class Alarm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner spinner1;
    private JSpinner spinner2;
    private JCheckBox ampm;

    private boolean auxAmpm;
    private Config config;

    public Alarm(boolean mode24, Config config) {
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);
        this.config = config;


        spinner1.setModel(new SpinnerNumberModel(0, 0, 11, 1));
        spinner2.setModel(new SpinnerNumberModel(0, 0, 59,1));
        ampm.setText(" AM");
        if (mode24){
            spinner1.setModel(new SpinnerNumberModel(0, 0, 23, 1));
            ampm.setVisible(false);
        }
        spinner1.setValue(Integer.valueOf(config.alarm.substring(0,2)));
        spinner2.setValue(Integer.valueOf(config.alarm.substring(3,5)));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        ampm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                auxAmpm = !auxAmpm;
                ampm.setText(auxAmpm ? " PM" : " AM");
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onOK() {
        this.config.alarm = String.format("%02d:%02d", spinner1.getValue(), spinner2.getValue());
        if (ampm.isVisible())
            this.config.alarm = this.config.alarm + ampm.getText();
        try {
            FileWriter fw  = new FileWriter("resources/config.json");
            Gson gson = new Gson();
            fw.write(gson.toJson(config));
            fw.close();
        }catch (Exception e){
            System.out.println("Error en guardar la fecha");
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(boolean mode, Config config) {
        Alarm dialog = new Alarm(mode, config);
        dialog.setUndecorated(true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

}
