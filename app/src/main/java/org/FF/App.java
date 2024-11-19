package org.FF;

public class App {
   public static void main(String[] args) {
        AltTabBlocker blocker = null;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                System.out.println("Windows");
                blocker = new WindowsAltTabBlocker();
            } else if (os.contains("linux")) {
                // Check for Wayland
                String xdgSessionType = System.getenv("XDG_SESSION_TYPE");
                if ("wayland".equals(xdgSessionType)) {
                    System.out.println("Wayland");
                    String desktopSession = System.getenv("XDG_CURRENT_DESKTOP");
                    if (desktopSession != null) {
                        if (desktopSession.toLowerCase().contains("gnome")) {
                            System.out.println("Gnome");
                            blocker = new GnomeWaylandBlocker();
                        } else if (desktopSession.toLowerCase().contains("kde")) {
                            // gnome forever
                            System.out.println("Gnome forever");
                        } else {
                            System.err.println("Unsupported Wayland desktop environment: " + desktopSession);
                            System.err.println("Only GNOME and KDE are currently supported under Wayland.");
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Unknown desktop environment under Wayland");
                        System.exit(1);
                    }
                } else {
                    System.err.println("Unsupported session type: " + xdgSessionType);
                    // Fallback to X11
                    // blocker = new LinuxX11Blocker();
                    // wayland forever
                }
            } else {
                System.err.println("Unsupported operating system");
                System.exit(1);
            }
            
            blocker.start();
            
            // Add shutdown hook to ensure cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (blocker != null) {
                    blocker.stop();
                }
            }));
            
            System.out.println("Alt+Tab blocking is active. Press Ctrl+C to exit.");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
            if (blocker != null) {
                blocker.stop();
            }
        } 
    }
}
