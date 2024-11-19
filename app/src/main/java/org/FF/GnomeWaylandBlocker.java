package org.FF;

import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;
import java.util.Map;
import java.util.HashMap;

public class GnomeWaylandBlocker implements AltTabBlocker {
    private DBusConnection connection;
    private boolean originalSwitcherEnabled;
    
    @Override
    public void start() {
        try {
            connection = DBusConnectionBuilder.forSessionBus().build();
            
            // Create a method call to get the original state
            Properties props = connection.getRemoteObject(
            "org.gnome.Shell",
                "/org/gnome/Shell",
                Properties.class
            );
            
            // Store original state
            originalSwitcherEnabled = (Boolean) props.Get("org.gnome.Shell", "switcher-enabled");
            
            // Create a method call to disable Alt+Tab
            Map<String, Object> values = new HashMap<>();
            values.put("switcher-enabled", false);
            props.Set("org.gnome.Shell", "switcher-enabled", false);
            
        } catch (DBusException e) {
            throw new RuntimeException("Failed to connect to GNOME Shell", e);
        }
    }
    
    @Override
    public void stop() {
        try {
            if (connection != null) {
                // Restore original state
                Properties props = connection.getRemoteObject(
                    "org.gnome.Shell",
                    "/org/gnome/Shell",
                    Properties.class
                );
                props.Set("org.gnome.Shell", "switcher-enabled", originalSwitcherEnabled);
                connection.disconnect();
            }
        } catch (DBusException e) {
            e.printStackTrace();
        }
    }
}
