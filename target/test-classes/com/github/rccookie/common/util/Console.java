package com.github.rccookie.common.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.rccookie.common.util.Grid.GridElement;

import jline.TerminalFactory;

/**
 * Description: A console utility class.
 */
public final class Console {
    private Console() { }

    private static final char T_H = '-';//'\u2500'; // For Greenfoot console
    private static final char T_V = '\u2502';
    private static final char T_C = '\u253C';
    private static final char T_TL = '\u250C';
    private static final char T_TR = '\u2510';
    private static final char T_BL = '\u2514';
    private static final char T_BR = '\u2518';
    private static final char T_L = '\u251C';
    private static final char T_R = '\u2524';
    private static final char T_T = '\u252C';
    private static final char T_B = '\u2534';

    private static final int DEFAULT_CELL_WIDTH = 20;

    private static final char C_WHITE = ' ';
    private static final char C_LIGHT = '\u2591';
    private static final char C_MEDIUM = '\u2592';
    private static final char C_DARK = '\u2593';
    private static final char C_BLACK = '\u2588';

    //private static final String LOG_ID = ">>";

    private static final int PROGRESS_BAR_WIDTH = 10;
    private static final char PROB_START = '[';
    private static final char PROB_END = ']';
    private static final char PROB_ON = '.';

    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    public static boolean coloredOutput = true;

    public static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));



    public static final void table(final Map<?,?> map) {
        table(map, DEFAULT_CELL_WIDTH);
    }

    public static final void table(final Map<?,?> map, final int maxCellWidth) {
        final Table<Object> table = new Table<>(2, map.size() + 1);

        table.set(0, 0, "KEYS");
        table.set(1, 0, "VALUES");

        final Entry<?,?>[] entrys = map.entrySet().toArray(new Entry[0]);
        for(int i=0; i<map.size(); i++) {
            table.set(0, i + 1, entrys[i].getKey());
            table.set(1, i + 1, entrys[i].getValue());
        }
        table(table, 1, maxCellWidth);
    }


    public static final void table(final Table<?> table) {
        table(table, 1, DEFAULT_CELL_WIDTH);
    }

    public static final void table(final Table<?> table, final int minCellWidth, final int maxCellWidth) {
        table(table, minCellWidth, maxCellWidth, System.out);
    }

    public static final void table(final Table<?> table, final int minCellWidth, final int maxCellWidth, final PrintStream out) {
        if(table == null) return;
        final int r = table.rowCount(), c = table.columnCount();
        if(r == 0) return;

        final Table<String> sTable = new Table<>(r, c, loc -> {
            Object element = table.get(loc.row(), loc.column());
            return element != null ? element.toString() : null;}
        );

        final int[] cellWidths = new int[c];
        for(int i=0; i<c; i++) {
            cellWidths[i] = minCellWidth;
            cellLoop:
            for(final String cell : sTable.column(i)) {
                final int length = cell != null ? cell.length() : String.valueOf((String)null).length();
                if(length <= cellWidths[i]) continue cellLoop;
                if(length > maxCellWidth) {
                    cellWidths[i] = maxCellWidth;
                    break cellLoop;
                }
                cellWidths[i] = length;
            }
        }

        out.println(line(c, cellWidths, -1));
        for(int i=0; i<r; i++) {
            out.println(row(sTable.row(i), cellWidths));
            out.println(line(c, cellWidths, i+1 < r ? 0 : 1));
        }
    }

    private static final String line(final int cells, final int[] cellWidths, final int pos) {
        final StringBuilder string = new StringBuilder();
        
        if(pos < 0) string.append(T_TL);
        else if(pos > 0) string.append(T_BL);
        else string.append(T_L);

        for(int i=0; i<cells; i++) {
            string.append(T_H).append(T_H);
            for(int j=0; j<cellWidths[i]; j++) string.append(T_H);
            if(i+1 < cells) {
                if(pos < 0) string.append(T_T);
                else if(pos > 0) string.append(T_B);
                else string.append(T_C);
            }
            else if(pos < 0) string.append(T_TR);
            else if(pos > 0) string.append(T_BR);
            else string.append(T_R);
        }
        return string.toString();
    }

    private static final String row(final List<String> row, final int[] cellWidths) {
        final StringBuilder string = new StringBuilder();
        string.append(T_V);
        for(int i=0; i<row.size(); i++) {
            string.append(' ');
            String content = row.get(i);
            if(content == null) content = String.valueOf((String)null);
            for(int j=0; j<cellWidths[i]; j++) {
                if(j < content.length()) string.append(content.charAt(j));
                else string.append(' ');
            }
            string.append(' ').append(T_V);
        }
        return string.toString();
    }




    /**
     * Width should not be bigger the 90 pixels.
     */
    public static final void paint(final BufferedImage image) {
        paint(image, 1);
    }

    /**
     * Width should not be bigger the 90 pixels.
     */
    public static final void paint(final BufferedImage image, double scale) {
        paint(image, true, scale);
    }

    /**
     * Width should not be bigger the 90 pixels.
     */
    public static final void paint(final BufferedImage image, final boolean negative, double scale) {
        paint(image, negative, scale, System.out);
    }

    public static final void paint(final BufferedImage image, final boolean negative, double scale, final PrintStream out) {
        AffineTransform at = new AffineTransform();
        at.scale(2.4 * scale, scale);
        final BufferedImage scaledImage = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(image, new BufferedImage((int)(2.4 * image.getWidth() * scale), (int)(image.getHeight() * scale), BufferedImage.TYPE_INT_ARGB));
        paint(new Table<Integer>(scaledImage.getHeight(), scaledImage.getWidth(), loc -> toBrightness(scaledImage, loc, negative)), out);
    }

    private static final int toBrightness(final BufferedImage image, final GridElement<?> loc, final boolean negative) {
        final Color c = new Color(image.getRGB(loc.column(), loc.row()));
        final int brightness = (int)Math.sqrt(
            0.299 * c.getRed() * c.getRed() +
            0.587 * c.getGreen() * c.getGreen() +
            0.114 * c.getBlue() * c.getBlue()
        );
        return negative ? 255 - brightness : brightness;
    }

    public static final void paint(final Table<Integer> table) {
        paint(table, System.out);
    }

    public static final void paint(final Table<Integer> table, final PrintStream out) {
        if(table == null) return;
        for(int i=0; i<table.rowCount(); i++) {
            StringBuilder row = new StringBuilder();
            for(final int color : table.row(i)) row.append(colorChar(color));
            out.println(row);
        }
    }

    private static final char colorChar(final int color) {
        if(color < 32) return C_BLACK;
        if(color < 96) return C_DARK;
        if(color < 159) return C_MEDIUM;
        if(color < 223) return C_LIGHT;
        return C_WHITE;
    }



    public static final void newLine() {
        newLine(1);
    }

    public static final void newLine(int count) {
        newLine(count, System.out);
    }

    public static final void newLine(int count, PrintStream out) {
        for(int i=0; i<count; i++) out.println();
    }




    

    private static final String stringFor(Object o) {
        if(o == null) return String.valueOf((String)null);
        if(!(o.getClass().isArray())) return o.toString();
        if(o instanceof boolean[]) return Arrays.toString((boolean[])o);
        if(o instanceof double[]) return Arrays.toString((double[])o);
        if(o instanceof float[]) return Arrays.toString((float[])o);
        if(o instanceof long[]) return Arrays.toString((long[])o);
        if(o instanceof int[]) return Arrays.toString((int[])o);
        if(o instanceof short[]) return Arrays.toString((short[])o);
        if(o instanceof char[]) return Arrays.toString((char[])o);
        if(o instanceof byte[]) return Arrays.toString((byte[])o);
        return '[' + Arrays.stream((Object[])o).map(e -> stringFor(e)).collect(Collectors.joining(", ")) + ']';
    }


    /**
     * Clears the console.
     */
    public static final void clear() {
        savelyPrintln(String.format("\033[2J"));
    }



    private static boolean progressBarActive = false;
    private static int lastOn = -1;
    private static int lastPercentage = -1;

    /**
     * Sets the given progress on the console progress bar. If there
     * is no progress bar yet there will be created one.
     * 
     * @param progress The progress to set, a value between {@code 0}
     *                 and {@code 1}, inslusive
     */
    public static final void setProgress(double progress) {
        if(progress < 0) progress = 0;
        if(progress > 1) progress = 1;

        int percentage = (int)Math.round(progress * 100);
        if(percentage == lastPercentage) return;

        int on = (int)Math.round(PROGRESS_BAR_WIDTH * progress);

        StringBuilder dif = new StringBuilder();

        if(!progressBarActive) {
            dif.append('[').append(colored("INFO", Colors.BLUE)).append(']');
            dif.append(' ').append(PROB_START);
            for(int i=0; i<PROGRESS_BAR_WIDTH; i++) dif.append(i < on ? PROB_ON : ' ');
        }
        else {
            int percWidth = (lastPercentage + "").length();
            for(int i=0; i<percWidth+3; i++) dif.append('\b');
            
            if(on != lastOn) {
                int min = Math.min(on, lastOn);
                for(int i=PROGRESS_BAR_WIDTH; i>min; i--) dif.append('\b');

                for(int i=min; i<PROGRESS_BAR_WIDTH; i++) dif.append(i < on ? PROB_ON : ' ');
            }
        }

        dif.append(PROB_END + " " + percentage + "%");

        System.out.print(dif);

        lastOn = on;
        lastPercentage = percentage;
        if(percentage == 100) {
            System.out.println();
            progressBarActive = false;
        }
        else progressBarActive = true;
    }


    /**
     * Prints the current stack trace until this method call into the console.
     */
    public static final void printStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] reducedStackTrace = new StackTraceElement[stackTrace.length - 3];
        System.arraycopy(stackTrace, 2, reducedStackTrace, 0, reducedStackTrace.length);
        String stackString = Arrays.stream(reducedStackTrace).map(e -> e.toString()).collect(Collectors.joining("\n\t"));
        savelyPrintln(">>>" + '\t' + stackString);
    }


    /**
     * Prints a splitting line with the given title in the console.
     * 
     * @param title The title of the split
     */
    public static final void split(String title) {
        if(title == null) title = "null";

        int width = TerminalFactory.get().getWidth();

        StringBuilder out = new StringBuilder(width);

        int lineLength = width - (title.length() + 4);
        int firstHalf = width / 2, secondHalf = lineLength - firstHalf;

        for(int i=0; i<firstHalf; i++) out.append('-');
        out.append("< ").append(title).append(" >");
        for(int i=0; i<secondHalf; i++) out.append('-');

        savelyPrintln(out);
    }

    /**
     * Prints a splitting line in the console;
     */
    public static final void split() {
        int width = TerminalFactory.get().getWidth();
        StringBuilder line = new StringBuilder(width);
        for(int i=0; i<width; i++) line.append('-');
        savelyPrintln(line);
    }


    /**
     * Prints the given information in the console.
     * 
     * @param x The information to print
     */
    public static final void info(Object x) {
        info(new Object[] {x});
    }

    /**
     * Prints the given information in the console.
     * 
     * @param x An information to print
     * @param y Another information to print
     * @param more More information to print
     */
    public static final void info(Object x, Object y, Object... more) {
        info(combine(x, y, more));
    }

    /**
     * Prints the given warning in the console.
     * 
     * @param x The warning to print
     */
    public static final void warn(Object x) {
        warn(new Object[] {x});
    }

    /**
     * Prints the given warnings in the console.
     * 
     * @param x A warning to print
     * @param y Another warning to print
     * @param more More warnings to print
     */
    public static final void warn(Object x, Object y, Object... more) {
        warn(combine(x, y, more));
    }

    /**
     * Prints the given error in the console.
     * 
     * @param x The error to print
     */
    public static final void error(Object x) {
        error(new Object[] {x});
    }

    /**
     * Prints the given errors in the console.
     * 
     * @param x A error to print
     * @param y Another error to print
     * @param more More errors to print
     */
    public static final void error(Object x, Object y, Object... more) {
        error(combine(x, y, more));
    }

    /**
     * Logs the given information together with the current time
     * in the console.
     * 
     * @param x The information to print
     */
    public static final void log(Object x) {
        log(new Object[] {x});
    }

    /**
     * Logs the given information together with the current time
     * in the console.
     * 
     * @param x An information to print
     * @param y Another information to print
     * @param more More information to print
     */
    public static final void log(Object x, Object y, Object... more) {
        log(combine(x, y, more));
    }

    /**
     * Prints the two objects as key-value pair as information in
     * the console.
     * <p>For example,
     * <pre>{@code map("Hello", "World");}</pre>
     * will result in the output {@code [INFO] Hello: World}
     * 
     * @param key The key to map the value to
     * @param value The value to map
     */
    public static final void map(final Object key, final Object value) {
        info(new Object[] {key + ": " + value});
    }


    private static final Object[] combine(Object x, Object y, Object[] more) {
        if(more == null) return new Object[] {x, y, null};
        else if(more.length == 0) return new Object[] {x, y};
        final Object[] combined = new Object[more.length + 2];
        combined[0] = x;
        combined[1] = y;
        System.arraycopy(more, 0, combined, 2, more.length);
        return combined;
    }


    private static final void info(Object[] objects) {
        internalPrint("INFO", Colors.BLUE, objects);
    }

    private static final void warn(Object[] objects) {
        internalPrint("WARN", Colors.YELLOW, objects);
    }

    private static final void error(Object[] objects) {
        internalPrint("ERROR", Colors.RED, objects);
    }

    private static final void log(Object[] objects) {
        Calendar c = Calendar.getInstance();
        StringBuilder time = new StringBuilder(8);
        time.append(String.format("%02d", c.get(Calendar.HOUR_OF_DAY)));
        time.append(':');
        time.append(String.format("%02d", c.get(Calendar.MINUTE)));
        time.append(':');
        time.append(String.format("%02d", c.get(Calendar.SECOND)));
        internalPrint(time.toString(), Colors.GREEN, objects);
    }

    private static final void internalPrint(String title, Colors color, Object[] objects) {
        if(objects == null)
            objects = new Object[] {null};
            // Will print 'null'

        StringBuilder out = new StringBuilder();

        out.append('[').append(colored(title, color)).append(']');
        out.append(' ');
        out.append(Arrays.stream(objects).map(o -> stringFor(o)).collect(Collectors.joining(", ")));

        out.append(' ');

        String classAndLineString = classAndLineString(5);
        final int width = TerminalFactory.get().getWidth();
        int usedWidth = width - ((out.length() + classAndLineString.length() - (coloredOutput ? getAsciiColor(color).length() + RESET.length() : 0)) % width);
        for(int i=0; i!=usedWidth; i++) out.append(' ');
        out.append(classAndLineString);

        savelyPrintln(out);
    }


    private static final void savelyPrintln(Object x) {
        savelyPrint(x);
        System.out.println();
    }

    private static final void savelyPrint(Object x) {
        if(progressBarActive) {
            System.out.println();
            progressBarActive = false;
        }
        System.out.print(x);
    }


    public static final String input(String prompt) {
        if(prompt == null) prompt = "null";

        StringBuilder out = new StringBuilder();

        out.append('[').append(colored("INPUT", Colors.PURPLE)).append(']');
        out.append(' ').append(prompt).append(" ");

        savelyPrint(out);

        String result;
        try {
            result = reader.readLine();
        } catch(IOException e) {
            System.out.println();
            e.printStackTrace();
            result = null;
        }

        return result;
    }



    /**
     * Returns the given string colored in the specified color. If {@link coloredOutput}
     * is {@code false} or the color is {@code null}, the input string will be returned.
     * Note that the size of the string will increase by {@code 9} if the text actually
     * gets colored.
     * 
     * @param string The string to color
     * @param color The color to paint the string in
     * @return The painted string
     */
    public static final String colored(String string, Colors color) {
        if(!coloredOutput || color == null) return string;
        return new StringBuilder(string.length() + 2).append(getAsciiColor(color)).append(string).append(RESET).toString();
    }

    private static final String classAndLineString(int off) {
        final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        final int index = elements.length > off ? off : elements.length - 1;
        return elements[index].getFileName() + ':' + elements[index].getLineNumber();
    }

    private static final String getAsciiColor(Colors color) {
        switch (color) {
            case WHITE: return WHITE;
            case YELLOW: return YELLOW;
            case RED: return RED;
            case PURPLE: return PURPLE;
            case BLUE: return BLUE;
            case CYAN: return CYAN;
            case GREEN: return GREEN;
            case BLACK: return BLACK;
        }
        throw new RuntimeException("Unexpected color");
    }

    /**
     * A set of colors that can be chosen to color text in using {@code colored(String, Colors)}.
     */
    public static enum Colors {
        WHITE,
        YELLOW,
        RED,
        PURPLE,
        BLUE,
        CYAN,
        GREEN,
        BLACK
    }


    public static void main(String[] args) {
        //test();
        map("Result", input("Enter something:"));
        map("Result", input("Enter something:"));
    }


    static void test() {
        info("Hello", "World");
        info((Object)new Object[] {"Hello", "World"});
        split("Type");
        info("Hello");
        warn("Hello");
        error("Hello");
        log("Hello");
        map("Reset size", RESET.length());
        printStackTrace();
        setProgress(0.5);
        setProgress(0.75);
    }
}