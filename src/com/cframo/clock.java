package com.cframo;

import com.google.gson.Gson;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class clock  implements Runnable{

    //GUI VAR
    private JPanel mainPanel;
    private JLabel time;
    private JLabel date;
    private JLabel itemAlarm;
    private JFrame frame;

    //LOGICAL VAR
    private Calendar calendar;
    private Timer timer;
    private int day;
    private boolean hour24;
    private boolean withDate;
    private boolean lang;
    private boolean haveAlarm;
    private boolean stop;
    public Thread watcherAlarm;

    //PRIV OBJ
    private Config config;

    public clock()
    {
        this.watcherAlarm = new Thread(this);
        config = new Config();
        init_custom();
        load_config();
        time();

        frame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE){
                    watcherAlarm = new Thread();
                    stop = true;
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {

                    case KeyEvent.VK_F1:
                        hour24 = !hour24;
                        if (watcherAlarm.isAlive())
                            stop = true;
                        config.convert_time(hour24);
                        save_config();
                        time();
                        showPopup(hour24 ? "MODE: 24": "MODE: 12", 2);
                        break;

                    case KeyEvent.VK_F2:
                        withDate = !withDate;
                        whatsDate();
                        save_config();
                        showPopup(withDate ? "DATE: ON": "DATE: OFF", 2);
                        break;

                    case KeyEvent.VK_F5:
                        showPopup("MIN", 1);
                        frame.setState(JFrame.ICONIFIED);
                        break;

                    case KeyEvent.VK_F3:
                        lang = !lang;
                        showPopup(lang ? "LANG: EN" : "LANG: ES", 2);
                        whatsDate();
                        save_config();
                        break;

                    case KeyEvent.VK_F6:
                        showPopup("ALARM", 1);
                        Alarm.main(hour24, config);
                        break;

                    case KeyEvent.VK_F7:
                        haveAlarm = !haveAlarm;
                        showPopup(haveAlarm ? "ALRM: ON" : "ALRM: OFF", 2);
                        save_config();
                        break;
                }
            }
        });
    }

    private void init_custom()
    {
        Frame_custom();
        Panel_custom();
        Label_custom();

        time.setText("");
        date.setText("");
        itemAlarm.setText("");

        frame.add(mainPanel);
    }

    //JFrame's config
    private void Frame_custom()
    {
        frame = new JFrame("Reloj");

        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            try {
                Toolkit xToolkit = Toolkit.getDefaultToolkit();
                Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(xToolkit, "Simple Clock");
            } catch (Exception ignored) { }
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        Image icon = Toolkit.getDefaultToolkit().getImage("resources/clock.png");
        frame.setIconImage(icon);
        frame.setVisible(true);
    }

    //JLabel's style
    private void Label_custom()
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        time.setForeground(Color.WHITE);
        time.setFont(new Font("Arial", 0, tk.getScreenResolution()+80));
        date.setForeground(Color.WHITE);
        date.setFont(new Font("Arial", 0, tk.getScreenResolution()-45));
        itemAlarm.setFont(new Font("Arial", Font.BOLD, tk.getScreenResolution()-70));
        itemAlarm.setForeground(Color.WHITE);
    }

    private void Panel_custom()
    {
        mainPanel.setBackground(Color.black);
    }

    private void load_config()
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader("resources/config.json"));
            Gson gson = new Gson();
            config = gson.fromJson(br, Config.class);

            this.hour24 = config.mode24;
            this.withDate = config.date;
            this.lang = config.lang;
            this.haveAlarm = config.haveAlarm;
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "ERROR: load_config");
        }
    }

    //Watch's functionality
    private void time()
    {
        calendar = new GregorianCalendar();

        try {
            timer.stop();
        }catch (Exception e){
            // :)
        }

        timer = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Date actual = new Date();
                calendar.setTime(actual);

                if (day != calendar.get(Calendar.DAY_OF_WEEK) && withDate)
                    whatsDate();

                itemAlarm.setText(haveAlarm ? "A" : "");

                if(hour24){
                    time.setText(
                            String.format("%02d:%02d:%02d",
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    calendar.get(Calendar.SECOND)));
                }else{
                    time.setText(
                            String.format("%02d:%02d:%02d %S",
                                    calendar.get(Calendar.HOUR),
                                    calendar.get(Calendar.MINUTE),
                                    calendar.get(Calendar.SECOND),
                                    calendar.get(Calendar.AM_PM)==Calendar.AM?"AM":"PM"));
                }
                if (haveAlarm)
                    verify_alarm();
            }
        });
        timer.start();
    }

    //Set date on screen & verify lang.
    private void whatsDate()
    {
        String[] days = new String[]{"Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"};

        if (!lang) {
            days = new String[]{"Domingo", "Lunes", "Martes", "Miércoles", "Viernes", "Viernes", "Sábado"};
            months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto",
                    "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        }

        Calendar calendar = new GregorianCalendar();
        day = calendar.get(Calendar.DAY_OF_WEEK);
        date.setText( withDate ?
                String.format("%S, %02d %S %d",
                        days[calendar.get(Calendar.DAY_OF_WEEK)-1],
                        calendar.get(Calendar.DAY_OF_MONTH),
                        months[calendar.get(Calendar.MONTH)],
                        calendar.get(Calendar.YEAR)) : "");
    }

    //Alarm's functionality
    private void verify_alarm()
    {
        if (time.getText().indexOf(config.alarm) == 0 || matches12() ){
            if (!watcherAlarm.isAlive() && !stop)
                watcherAlarm.start();
        }else{
            stop = false;
            watcherAlarm = new Thread(clock.this);
        }
    }

    //Play sound on new thread
    @Override
    public void run()
    {
        while (  ( time.getText().indexOf(config.alarm) == 0 || matches12() )&& !stop){
            try {
                Player apl = new Player(new FileInputStream("resources/sound.mp3"));
                apl.play();
            }catch (Exception e){
                JOptionPane.showMessageDialog(null, "ERROR: run");
            }
        }
    }

    //Check alarm in mode 12
    private boolean matches12()
    {
        if (hour24)
            return false;
        return time.getText().startsWith(config.getHour12()) && time.getText().endsWith(config.getAMPM());
    }

    private void save_config()
    {
        config.mode24 = hour24;
        config.date = withDate;
        config.haveAlarm = haveAlarm;
        try {
            FileWriter fw  = new FileWriter("resources/config.json");
            Gson gson = new Gson();
            fw.write(gson.toJson(config));
            fw.close();
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "ERROR: save_config");
        }
    }

    //Print little popup on screen
    private void showPopup(String msg, int time)
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        JDialog jd = new JDialog(frame, msg, true);
            //JDialog's Style
            jd.setSize(tk.getScreenResolution()*2, tk.getScreenResolution()/2);
            jd.setUndecorated(true);
            jd.setLocationRelativeTo(null);
        JButton bmsg = new JButton(msg);
            //JButton's style
            bmsg.setContentAreaFilled(false);
            bmsg.setBorderPainted(false);
            bmsg.setFocusPainted(false);
            bmsg.setFont(new Font("Arial", Font.BOLD, 25));
        jd.add(bmsg);
        Timer popup = new Timer(time*1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jd.setVisible(false);
                jd.dispose();
            }
        });
        popup.setRepeats(false);
        popup.start();
        jd.setVisible(true);
    }
}