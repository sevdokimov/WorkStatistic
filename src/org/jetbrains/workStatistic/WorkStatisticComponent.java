package org.jetbrains.workStatistic;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static java.awt.AWTEvent.*;

/**
 * @author Sergey Evdokimov
 */
public class WorkStatisticComponent implements ApplicationComponent {

  private static final int SESSION_EXPIRED_TIME = 5 * 1000;
  private static final int MAX_SESSION_TIME = 15 * 1000;

  private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

  private File file;

  private volatile boolean isClosed;

  private Timer timer;

  private long sessionStartTime;
  private long lastActivity;

  private final AWTEventListener listener = new AWTEventListener() {
    public void eventDispatched(AWTEvent event) {
      if (isClosed) return;

      long currentTime = System.currentTimeMillis();

      if (sessionStartTime == 0) {
        sessionStartTime = lastActivity = currentTime;
        return;
      }

      if (lastActivity + SESSION_EXPIRED_TIME < currentTime) {
        writeSessionData();
        sessionStartTime = lastActivity = currentTime;
        return;
      }

      lastActivity = currentTime;
    }
  };

  private void checkSessionExpired() {
    if (sessionStartTime == 0) return;

    if (lastActivity + SESSION_EXPIRED_TIME < System.currentTimeMillis()) {
      writeSessionData();
      sessionStartTime = 0;
      return;
    }

    if (lastActivity - sessionStartTime > MAX_SESSION_TIME) {
      writeSessionData();
      sessionStartTime = lastActivity;
    }
  }

  private void writeSessionData() {
    write(FORMAT.format(new Date(sessionStartTime)) + " work " + ((lastActivity - sessionStartTime) / 1000) + "." + (((lastActivity - sessionStartTime) / 10) % 100));
  }

  private void write(String s) {
    if (true) {
      System.out.println(s);
      return;
    }

    try {
      PrintStream out = new PrintStream(new FileOutputStream(file, true));

      try {
        out.println(s);
      }
      finally {
        out.close();
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void initComponent() {
    String log = System.getenv("WORK_STATISTIC_LOG");
    if (log == null) {
      log = SystemProperties.getUserHome() + "/workStatistic.log";
    }

    file = new File(log);

    write(FORMAT.format(new Date()) + " Startup");

    timer = new Timer(2000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (isClosed) return;

        checkSessionExpired();

        for (final Window window : Window.getWindows()) {
          Toolkit toolkit = window.getToolkit();

          if (!Arrays.asList(toolkit.getAWTEventListeners()).contains(listener)) {
            toolkit.addAWTEventListener(listener, MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK | KEY_EVENT_MASK);
          }
        }
      }
    });

    timer.start();
  }

  public void disposeComponent() {
    isClosed = true;
    timer.stop();
    if (sessionStartTime != 0) {
      writeSessionData();
    }

    write(FORMAT.format(new Date()) + " Shutdown");
  }

  @NotNull
  public String getComponentName() {
    return "WorkStatistic";
  }
}
