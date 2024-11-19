package org.FF;


import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.Pointer;

public class WindowsAltTabBlocker implements AltTabBlocker {
    private WinUser.HHOOK keyboardHook;
    private final User32 lib = User32.INSTANCE;
    private WinUser.LowLevelKeyboardProc keyboardProc;

    @Override
    public void start() {
        keyboardProc = (nCode, wParam, info) -> {
            if (nCode >= 0) {
                WinUser.KBDLLHOOKSTRUCT kbd = new WinUser.KBDLLHOOKSTRUCT(info.getPointer());
                
                // Check if Alt is pressed
                boolean altPressed = (lib.GetAsyncKeyState(0x12) & 0x8000) != 0; // 0x12 is VK_MENU (Alt key)
                
                // If Alt+Tab combination is detected
                if (altPressed && kbd.vkCode == 0x09) { // 0x09 is VK_TAB
                    return new LRESULT(1);
                }
            }
            return lib.CallNextHookEx(keyboardHook, nCode, new WPARAM(wParam.longValue()), info);
        };

        HWND hWnd = null;
        keyboardHook = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardProc, null, 0);
    }

    @Override
    public void stop() {
        if (keyboardHook != null) {
            lib.UnhookWindowsHookEx(keyboardHook);
            keyboardHook = null;
        }
    }
}

