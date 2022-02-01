package hu.codelens.sharecenter.internal;

import hu.codelens.sharecenter.ColorOutput;

import java.io.PrintStream;

public class DefaultColorOutput implements ColorOutput {

    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String WHITE = "\u001B[37m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String YELLOW = "\u001B[33m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    private static final String DOT = "\u25cf";
    private static final String PROG_FILL = "\u2588";
    private static final String PROG_EMPTY = "\u2591";

    private static final String[] SPINNER = {"-", "\\", "|", "/"};
    private static int SPINNER_OFFSET = 0;

    private PrintStream output;

    public DefaultColorOutput() {
        output = System.out;
    }

    @Override
    public PrintStream getOutput() {
        return output;
    }

    @Override
    public void setOutput(PrintStream output) {
        this.output = output;
    }

    @Override
    public void nl() {
        output.println();
    }

    @Override
    public void reset() {
        output.print(RESET);
    }

    @Override
    public void black() {
        output.print(BLACK);
    }

    @Override
    public void white() {
        output.print(WHITE);
    }

    @Override
    public void red() {
        output.print(RED);
    }

    @Override
    public void green() {
        output.print(GREEN);
    }

    @Override
    public void blue() {
        output.print(BLUE);
    }

    @Override
    public void yellow() {
        output.print(YELLOW);
    }

    @Override
    public void purple() {
        output.print(PURPLE);
    }

    @Override
    public void cyan() {
        output.print(CYAN);
    }

    @Override
    public void lineReturn() {
        output.print("\r");
    }

    @Override
    public void dot() {
        output.print(DOT);
    }

    @Override
    public String getDot() {
        return DOT;
    }

    @Override
    public void dotLine(String line) {
        dot();
        output.print(" " + line);
    }

    @Override
    public void dotLineLn(String line) {
        dotLine(line);
        output.println();
    }

    @Override
    public void withGreen(String text) {
        green();
        output.print(text);
        reset();
    }

    @Override
    public void withGreenLn(String text) {
        green();
        output.println(text);
        reset();
    }

    @Override
    public void withRed(String text) {
        red();
        output.print(text);
        reset();
    }

    @Override
    public void withRedLn(String text) {
        red();
        output.println(text);
        reset();
    }

    @Override
    public void withYellow(String text) {
        yellow();
        output.print(text);
        reset();
    }

    @Override
    public void withYellowLn(String text) {
        yellow();
        output.println(text);
        reset();
    }

    @Override
    public void progress(int len, int percentage) {
        progress(0, len, percentage);
    }

    @Override
    public void progress(int padding, int len, int percentage) {
        progress(padding, len, percentage, false);
    }

    @Override
    public void progress(int padding, int len, int percentage, boolean spinner) {
        int filled = (int) ((float) (len) * ((float) percentage / 100f));
        int empty = len - filled;
        if (empty + filled < len) {
            empty = 0;
            filled = len;
        }

        lineReturn();
        for (int index = 0; index < padding; index++) {
            output.print(" ");
        }

        for (int index = 0; index < filled; index++) {
            output.print(PROG_FILL);
        }
        for (int index = 0; index < empty; index++) {
            output.print(PROG_EMPTY);
        }

        output.printf(" %02d%% %s", percentage, spinner ? getSpinner() : "  ");
    }

    private static synchronized String getSpinner() {
        if (SPINNER_OFFSET >= SPINNER.length) {
            SPINNER_OFFSET = 0;
        }
        return SPINNER[SPINNER_OFFSET++];
    }
}
