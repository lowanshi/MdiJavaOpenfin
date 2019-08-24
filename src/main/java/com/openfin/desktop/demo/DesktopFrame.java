package com.openfin.desktop.demo;// Fig. 22.11: DesktopFrame.java
// Demonstrating JDesktopPane.
import com.openfin.desktop.*;
import com.openfin.desktop.Window;
import com.sun.jna.Native;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.System;
import java.lang.reflect.Field;
import java.util.UUID;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

import static javax.swing.plaf.basic.BasicInternalFrameTitlePane.*;

/**
 * Created by Praveen Lowanshi on 2/24/2019.
 */
public class DesktopFrame extends JFrame
{
   private JDesktopPane theDesktop;
   private String openfin_app_url = "https://www.google.com";

   protected DesktopConnection desktopConnection;

   public DesktopFrame() {
      super("Using a JDesktopPane");
      try {
         this.desktopConnection = new DesktopConnection(appUuid);
      } catch (DesktopException desktopError) {
         desktopError.printStackTrace();
      }

      JMenu menu, submenu;
      JMenuItem i1, i2, i3, i4, i5;

      startOpenFinRuntime();
      JMenuBar bar = new JMenuBar(); // create menu bar
      menu = new JMenu("Menu");
      submenu = new JMenu("Sub Menu");
      i1 = new JMenuItem("Item 1");
      i2 = new JMenuItem("Item 2");
      i3 = new JMenuItem("Item 3");
      i4 = new JMenuItem("Item 4");
      i5 = new JMenuItem("Item 5");
      menu.add(i1);
      menu.add(i2);
      menu.add(i3);
      submenu.add(i4);
      submenu.add(i5);
      menu.add(submenu);
      bar.add(menu);
      setJMenuBar(bar); // set menu bar for this application

      embedCanvas = new java.awt.Canvas();
      embedCanvas.setFocusable(true);

      //create a hidden frame to hold the openfin window when it's iconified.
      hiddenFrame = new JFrame();
      hiddenFrame.setPreferredSize(new Dimension(640, 480));
      hiddenFrame.pack();
      hiddenFrame.setVisible(false);

      frame = new JInternalFrame("Internal Frame", true, true, true, true);

      BasicInternalFrameTitlePane titlePane = (BasicInternalFrameTitlePane) ((BasicInternalFrameUI) frame.getUI()).getNorthPane();
      // Class clsTitlePane = titlePane.class.getDeclaredField();

      frame.add(embedCanvas, BorderLayout.CENTER);
      frame.setSize(1000, 1000);
      theDesktop = new JDesktopPane(); // create desktop pane
      theDesktop.add(frame); // attach internal frame
      frame.pack();
      frame.setVisible(true); // show internal frame
      frame.setIconifiable(false);
      //when mixing lightweight and heavyweight component
      frame.addComponentListener(new ComponentListener() {
         private void revalidateWorkaround() {
            theDesktop.revalidate();
            DesktopFrame.this.validate();
         }

         @Override
         public void componentResized(ComponentEvent e) {
            revalidateWorkaround();
         }

         @Override
         public void componentMoved(ComponentEvent e) {
            revalidateWorkaround();
         }

         @Override
         public void componentShown(ComponentEvent e) {
            revalidateWorkaround();
         }

         @Override
         public void componentHidden(ComponentEvent e) {
            revalidateWorkaround();
         }
      });
      frame.addInternalFrameListener(new InternalFrameAdapter() {
         @Override
         public void internalFrameDeiconified(InternalFrameEvent e) {
            //when internal frame is restored, re-embed openfin window back to canvas
            long canvasHWndId = Native.getComponentID(embedCanvas);
            if (startupHtml5app != null) {
               startupHtml5app.getWindow().embedInto(canvasHWndId, embedCanvas.getWidth(), embedCanvas.getHeight(), new AckListener() {
                  @Override
                  public void onSuccess(Ack ack) {
                  }

                  @Override
                  public void onError(Ack ack) {
                  }
               });
            }
         }
      });
      try {
         Field f1 = BasicInternalFrameTitlePane.class.getDeclaredField("iconifyAction");
         f1.setAccessible(true);
         AbstractAction myAction = new BTSInofied(frame);
         Field iconButton = BasicInternalFrameTitlePane.class.getDeclaredField("iconButton");
         iconButton.setAccessible(true);

         NoFocusButton btsIconButton = new NoFocusButton(
                 "InternalFrameTitlePane.iconifyButtonAccessibleName",
                 "InternalFrameTitlePane.iconifyButtonOpacity");
         btsIconButton.addActionListener(myAction);
         titlePane.add(btsIconButton);
         iconButton.set(titlePane, btsIconButton);
         Icon iconIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
         btsIconButton.setIcon(iconIcon);
         btsIconButton.setToolTipText("Minimize");
         Boolean paintActive = frame.isSelected() ? Boolean.TRUE : Boolean.FALSE;
         btsIconButton.putClientProperty("paintActive", paintActive);
         Border handyEmptyBorder = new EmptyBorder(0, 0, 0, 0);

         btsIconButton.setBorder(handyEmptyBorder);
         btsIconButton.setContentAreaFilled(false);
         myAction.setEnabled(true);
         f1.set(titlePane, myAction);
         myAction.setEnabled(true);
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }


      embedCanvas.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent event) {
            super.componentResized(event);
            Dimension newSize = event.getComponent().getSize();
            try {
               if (startupHtml5app != null) {
                  startupHtml5app.getWindow().embedComponentSizeChange((int) newSize.getWidth(), (int) newSize.getHeight());
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });


      add(theDesktop); // add desktop pane to frame

   } // end constructor DesktopFrame

   private void launchHTMLApps() {
      // launch 5 instances of same example app
      int width = 300, height=200;
      int gap = 50;  // gap between windows at initial positions

      try {
         String uuid = UUID.randomUUID().toString();
         ApplicationOptions options = new ApplicationOptions(uuid, uuid, openfin_app_url);
         WindowOptions mainWindowOptions = new WindowOptions();
         mainWindowOptions.setAutoShow(true);
         mainWindowOptions.setFrame(false);
         mainWindowOptions.setResizable(false);
         mainWindowOptions.setDefaultHeight(height);
         mainWindowOptions.setDefaultTop(50);
         mainWindowOptions.setDefaultWidth(width);
         mainWindowOptions.setShowTaskbarIcon(true);
         mainWindowOptions.setSaveWindowState(false);  // set to false so all windows start at same initial positions for each run
         options.setMainWindowOptions(mainWindowOptions);
         launchHTMLApp(options, new AckListener() {
            @Override
            public void onSuccess(Ack ack) {
               Application app = (Application) ack.getSource();
               try {
                  embedStartupApp(app,frame);
               } catch (Exception ex) {
                  ex.printStackTrace();
               }
            }
            @Override
            public void onError(Ack ack) {
               // logger.error(String.format("Error launching %s %s", options.getUUID(), ack.getReason()));
            }
         });
      } catch (Exception e) {
         // logger.error("Error launching app", e);
      }
   }

   private void launchHTMLApp(ApplicationOptions options, AckListener ackListener) throws Exception {
      //logger.debug(String.format("Launching %s", options.getUUID()));
      DemoUtils.runApplication(options, this.desktopConnection, ackListener);
   }
   private void startOpenFinRuntime() {
      try {
         DesktopStateListener listener = new DesktopStateListener() {
            @Override
            public void onReady() {
               onRuntimeReady();
            }

            private void onRuntimeReady() {
               // addMenu.setEnabled(true);
               launchHTMLApps();

            }

            @Override
            public void onClose() {
               // updateMessagePanel(String.format("Connection closed %s", error));
            }
            @Override
            public void onError(String reason) {
               //updateMessagePanel("Connection failed: " + reason);
            }
            @Override
            public void onMessage(String message) {
            }
            @Override
            public void onOutgoingMessage(String message) {
            }
         };
         RuntimeConfiguration configuration = new RuntimeConfiguration();
         configuration.setDevToolsPort(9090);
         configuration.setAdditionalRuntimeArguments(" --v=1 "); // enable additional logging from Runtime
         String desktopVersion = java.lang.System.getProperty("com.openfin.demo.version");
         if (desktopVersion == null) {
            desktopVersion = "stable";
         }
         configuration.setRuntimeVersion(desktopVersion);
         desktopConnection.connect(configuration, listener, 60);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   private Application startupHtml5app;
   protected String appUuid = "JavaEmbedding";
   protected String startupUuid = "OpenFinHelloWorld";
   protected java.awt.Canvas embedCanvas;
   protected Long previousPrarentHwndId;
   JFrame hiddenFrame;
   JInternalFrame frame;
   private void embedStartupApp(Application app,JInternalFrame frame) {
      try {
         if (app == null) {
            app = Application.wrap(this.startupUuid, this.desktopConnection);
         }
         startupHtml5app =app;
         Window html5Wnd = app.getWindow();
         long parentHWndId = Native.getComponentID(this.embedCanvas);
         System.out.println("Canvas HWND " + Long.toHexString(parentHWndId));
         html5Wnd.embedInto(parentHWndId, this.embedCanvas.getWidth(), this.embedCanvas.getHeight(), new AckListener() {
            @Override
            public void onSuccess(Ack ack) {
               frame.setIconifiable(true);
               if (ack.isSuccessful()) {
                  previousPrarentHwndId = ack.getJsonObject().getLong("hWndPreviousParent");
               } else {
                  java.lang.System.out.println("embedding failed: " + ack.getJsonObject().toString());
               }
            }
            @Override
            public void onError(Ack ack) {
               frame.setIconifiable(true);
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private class BTSInofied extends AbstractAction {
      JInternalFrame frame;
      public BTSInofied(JInternalFrame frame)   {
         super();
         this.frame = frame;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         if(frame.isIconifiable()) {
            if(!frame.isIcon()) {
               long hiddenFrameHWndId = Native.getComponentID(hiddenFrame);
               if(startupHtml5app!=null) {
                  startupHtml5app.getWindow().embedInto(hiddenFrameHWndId, 640, 480, new AckListener() {
                     @Override
                     public void onSuccess(Ack ack) {
                        try {
                           frame.setIcon(true);
                        } catch (PropertyVetoException e) {
                           e.printStackTrace();
                        }
                     }

                     @Override
                     public void onError(Ack ack) {
                        try {
                           frame.setIcon(true);
                        } catch (PropertyVetoException e1) {
                           e1.printStackTrace();
                        }
                     }
                  });
               }

            } else{
               try { frame.setIcon(false); } catch (PropertyVetoException e1) { }
            }
         }
      }
   }

   private class NoFocusButton extends JButton {
      private String uiKey;
      public NoFocusButton(String uiKey, String opacityKey) {
         setFocusPainted(false);
         setMargin(new Insets(0,0,0,0));
         this.uiKey = uiKey;

         Object opacity = UIManager.get(opacityKey);
         if (opacity instanceof Boolean) {
            setOpaque(((Boolean)opacity).booleanValue());
         }
      }
      public boolean isFocusTraversable() { return false; }
      public void requestFocus() {}
      public AccessibleContext getAccessibleContext() {
         AccessibleContext ac = super.getAccessibleContext();
         if (uiKey != null) {
            ac.setAccessibleName(UIManager.getString(uiKey));
            uiKey = null;
         }
         return ac;
      }
   }  // end NoFocusButton

}




