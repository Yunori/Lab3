package com.pacman;


import com.pacman.engine.InputManager;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        PacManGameGUI game = new PacManGameGUI();
        KeyListener listener;
        try {
            PipedOutputStream pipedOut = new PipedOutputStream();
            InputStream in = new PipedInputStream(pipedOut);
            final DataOutputStream out = new DataOutputStream(pipedOut);
            InputManager.enable(in);
            listener = new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {
                    try {
                        out.writeInt(e.getKeyCode());
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {

                }
            };
            game.playGame(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
