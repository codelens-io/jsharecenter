package hu.codelens.sharecenter;

import hu.codelens.sharecenter.internal.DefaultColorOutput;

import java.io.PrintStream;

public interface ColorOutput {

    static ColorOutput getDefault() {
        return new DefaultColorOutput();
    }

    PrintStream getOutput();

    void setOutput(PrintStream output);

    void nl();

    void reset();

    void black();

    void white();

    void red();

    void green();

    void blue();

    void yellow();

    void purple();

    void cyan();

    void lineReturn();

    void dot();

    String getDot();

    void dotLine(String line);

    void dotLineLn(String line);

    void withGreen(String text);

    void withGreenLn(String text);

    void withRed(String text);

    void withRedLn(String text);

    void withYellow(String text);

    void withYellowLn(String text);

    void progress(int len, int percentage);

    void progress(int padding, int len, int percentage);

    void progress(int padding, int len, int percentage, boolean spinner);
}
