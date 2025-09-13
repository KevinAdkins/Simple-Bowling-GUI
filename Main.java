import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static class BowlingGame {
        private final int[] rolls = new int[21 + 3];
        private int currentRoll = 0;

        void roll(int pins) {
            if (pins < 0 || pins > 10) throw new IllegalArgumentException("Invalid pins: " + pins);
            rolls[currentRoll++] = pins;
        }

        int[] frameScores() {
            int[] cumul = new int[10];
            int rollIndex = 0;
            int running = 0;

            for (int frame = 0; frame < 10; frame++) {
                if (isStrike(rollIndex)) {
                    int frameScore = 10 + rolls[rollIndex + 1] + rolls[rollIndex + 2];
                    running += frameScore;
                    cumul[frame] = running;
                    rollIndex += 1;
                } else if (isSpare(rollIndex)) {
                    int frameScore = 10 + rolls[rollIndex + 2];
                    running += frameScore;
                    cumul[frame] = running;
                    rollIndex += 2;
                } else {
                    int frameScore = rolls[rollIndex] + rolls[rollIndex + 1];
                    running += frameScore;
                    cumul[frame] = running;
                    rollIndex += 2;
                }

                if (frame == 9) {
                }
            }
            return cumul;
        }

        private boolean isStrike(int i) { return rolls[i] == 10; }

        private boolean isSpare(int i) { return rolls[i] + rolls[i + 1] == 10; }
    }

    static class BowlingParser {

        static int[] parseToRolls(String line) {
            line = line.trim();

            if (line.matches("^[0-9 ]+$")) {
                String[] parts = line.trim().split("\\s+");
                int[] out = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    out[i] = Integer.parseInt(parts[i]);
                }
                return out;
            }

            List<Integer> rolls = new ArrayList<>();
            int prevPinsInFrame = -1;

            for (int idx = 0; idx < line.length(); idx++) {
                char c = line.charAt(idx);
                if (c == ' ' || c == '|') continue;

                if (c == 'X' || c == 'x') {
                    rolls.add(10);
                    prevPinsInFrame = -1;
                } else if (c == '-') {
                    rolls.add(0);
                    if (prevPinsInFrame == -1) prevPinsInFrame = 0;
                    else prevPinsInFrame = -1;
                } else if (c == '/') {
                    if (prevPinsInFrame < 0 || prevPinsInFrame > 10) {
                        throw new IllegalArgumentException("Spare '/' without a valid previous roll.");
                    }
                    int sparePins = 10 - prevPinsInFrame;
                    rolls.add(sparePins);
                    prevPinsInFrame = -1;
                } else if (Character.isDigit(c)) {
                    int pins = c - '0';
                    rolls.add(pins);
                    if (prevPinsInFrame == -1) prevPinsInFrame = pins;
                    else prevPinsInFrame = -1;
                } else {
                    throw new IllegalArgumentException("Unrecognized char: " + c);
                }
            }

            int[] out = new int[rolls.size()];
            for (int i = 0; i < rolls.size(); i++) out[i] = rolls.get(i);
            return out;
        }

        static BowlingGame buildGameFromLine(String line) {
            int[] rolls = parseToRolls(line);
            BowlingGame g = new BowlingGame();
            for (int r : rolls) g.roll(r);
            return g;
        }
    }

    static void runConsole() {
        System.out.println("Bowling Scorer (console). Enter a line of rolls (notation or integers).");
        System.out.println("Examples:");
        System.out.println("  Notation:  X X X X X X X X X XXX");
        System.out.println("             5/ 5/ 5/ 5/ 5/ 5/ 5/ 5/ 5/ 5/5");
        System.out.println("             9- 9- 9- 9- 9- 9- 9- 9- 9- 9-");
        System.out.println("  Integers:  10 10 10 10 10 10 10 10 10 10 10 10");
        System.out.println();
        System.out.print("Input: ");

        try {
            byte[] buf = new byte[4096];
            int n = System.in.read(buf);
            if (n <= 0) return;
            String line = new String(buf, 0, n).trim();

            BowlingGame game = BowlingParser.buildGameFromLine(line);
            int[] frames = game.frameScores();

            System.out.println("\nFrame-by-frame cumulative scores:");
            for (int i = 0; i < frames.length; i++) {
                System.out.printf("Frame %2d: %d%n", (i + 1), frames[i]);
            }
            System.out.printf("TOTAL: %d%n", frames[9]);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    static class BowlingGUI extends JFrame {
        private final JTextField inputField = new JTextField("X X X X X X X X X XXX");
        private final JTextArea outputArea = new JTextArea(10, 50);

        BowlingGUI() {
            super("Bowling Scorer");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBorder(new EmptyBorder(12, 12, 12, 12));

            JLabel lbl = new JLabel("Enter rolls (notation or integers):");
            JButton calc = new JButton(new AbstractAction("Calculate") {
                @Override public void actionPerformed(ActionEvent e) {
                    calculate();
                }
            });

            JPanel top = new JPanel(new BorderLayout(8, 8));
            top.add(lbl, BorderLayout.NORTH);
            top.add(inputField, BorderLayout.CENTER);
            top.add(calc, BorderLayout.EAST);

            outputArea.setEditable(false);
            outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

            root.add(top, BorderLayout.NORTH);
            root.add(new JScrollPane(outputArea), BorderLayout.CENTER);

            setContentPane(root);
            pack();
            setLocationRelativeTo(null);
        }

        private void calculate() {
            String line = inputField.getText().trim();
            try {
                BowlingGame game = BowlingParser.buildGameFromLine(line);
                int[] frames = game.frameScores();

                StringBuilder sb = new StringBuilder();
                sb.append("Frame-by-frame cumulative scores:\n");
                for (int i = 0; i < frames.length; i++) {
                    sb.append(String.format("Frame %2d: %d%n", (i + 1), frames[i]));
                }
                sb.append(String.format("TOTAL: %d%n", frames[9]));
                outputArea.setText(sb.toString());
            } catch (Exception ex) {
                outputArea.setText("Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        boolean console = args != null && args.length > 0 && "--console".equalsIgnoreCase(args[0]);
        if (console) {
            runConsole();
        } else {
            SwingUtilities.invokeLater(() -> new BowlingGUI().setVisible(true));
        }
    }
}
