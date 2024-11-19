package com.alttabblocker;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    private static Robot robot;
    private static JFrame blockerFrame;
    private static boolean isBlocking = false;

    public static void main(String[] args) {
        // Check operating system
        String os = System.getProperty("os.name").toLowerCase();

        try {
            // Initialize Robot for key simulation
            robot = new Robot();

            // Create the blocking mechanism
            setupBlockerFrame();

            // Platform-specific initialization
            if (os.contains("windows")) {
                initializeWindowsBlocking();
            } else if (os.contains("linux")) {
                initializeLinuxBlocking();
            } else {
                showErrorDialog("Unsupported operating system");
                System.exit(1);
            }

        } catch (AWTException e) {
            showErrorDialog("Could not initialize key blocking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupBlockerFrame() {
        blockerFrame = new JFrame("Alt+Tab Blocker");
        blockerFrame.setUndecorated(true);
        blockerFrame.setOpacity(0.01f);
        blockerFrame.setAlwaysOnTop(true);

        // Add global key listener
        blockerFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                // Block Alt+Tab
                if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_TAB) {
                    e.consume(); // Consume the event
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_TAB);
                }
            }
        });

        blockerFrame.setSize(1, 1);
        blockerFrame.setLocationRelativeTo(null);
        blockerFrame.setVisible(true);
    }

    private static void initializeWindowsBlocking() {
        // Windows-specific blocking setup
        try {
            // Create a native DLL for more robust blocking on Windows
            createWindowsNativeLibrary();
        } catch (Exception e) {
            showErrorDialog("Windows blocking setup failed: " + e.getMessage());
        }
    }

    private static void initializeLinuxBlocking() {
        // Linux GNOME Wayland blocking setup
        try {
            // Check if running on Wayland
            String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
            if (waylandDisplay == null || waylandDisplay.isEmpty()) {
                showErrorDialog("Not running on Wayland");
                System.exit(1);
            }

            // Create a custom Wayland input blocking script
            createLinuxBlockingScript();
        } catch (Exception e) {
            showErrorDialog("Linux blocking setup failed: " + e.getMessage());
        }
    }

    private static void createWindowsNativeLibrary() throws Exception {
        // Placeholder for Windows native DLL creation
        // In a real implementation, this would involve:
        // 1. Writing a C++ DLL that hooks into Windows input handling
        // 2. Compiling the DLL for different Windows architectures
        // 3. Loading the appropriate DLL based on system architecture
        System.out.println("Windows native blocking library would be created here");
    }

    private static void createLinuxBlockingScript() throws Exception {
        // Create a shell script to intercept and block Alt+Tab in Wayland
        String scriptContent = "#!/bin/bash\n" +
                "# Block Alt+Tab in Wayland\n" +
                "while true; do\n" +
                "  xdotool key Alt+Tab\n" +
                "  sleep 0.1\n" +
                "done";

        // Ensure script directory exists
        Files.createDirectories(Paths.get(System.getProperty("user.home") + "/.local/bin"));

        // Write the script
        Files.write(
                Paths.get(System.getProperty("user.home") + "/.local/bin/alt-tab-blocker.sh"),
                scriptContent.getBytes()
        );

        // Make script executable
        new File(System.getProperty("user.home") + "/.local/bin/alt-tab-blocker.sh")
                .setExecutable(true);
    }

    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Alt+Tab Blocker Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // Graceful shutdown method
    public static void stopBlocking() {
        if (blockerFrame != null) {
            blockerFrame.dispose();
        }
    }
}