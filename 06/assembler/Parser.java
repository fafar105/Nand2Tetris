import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static final String DEST = ".hack";
    public static final String A_TAG = "@";
    public static final String L_BEGIN_TAG = "(";
    public static final String L_END_TAG = ")";
    public static final String DEST_SEPARATOR = "=";
    public static final String JUMP_SEPARATOR = ";";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String COMMENT_TAG = "//";
    public static final String NUMBERS = "[0-9]*";

    public enum CommandType {
        A_COMMAND, C_COMMAND, L_COMMAND
    }

    private String currInstruct;
    private String filePath;
    private String fileName;

    private int currInsNumber = -1;
    private int totalNumber = -1;

    private final Map<Integer, String> sequence = new LinkedHashMap<>();

    private CommandType currCmd;

    private final SymbolTable table;
    private final Code code;

    public Parser(final String path, final SymbolTable table, final Code code) {
        this.table = table;
        this.code = code;
        init(path);
    }

    private void init(String path) {
        File file = new File(path);

        String name = file.getName();
        fileName = name.substring(0, name.lastIndexOf('.'));

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fileReader != null) {
            bufferedReader = new BufferedReader(fileReader);
        }

        String current;
        int lineNum = -1;
        try {
            if (bufferedReader != null) {
                while ((current = bufferedReader.readLine()) != null) {
                    if (current.startsWith(COMMENT_TAG) || current.trim().length() < 1) {
                        continue;
                    }
                    if (current.indexOf(COMMENT_TAG) > 0) {
                        current = current.substring(0, current.indexOf(COMMENT_TAG)).trim();
                    }
                    current = current.trim();
                    if (current.startsWith(L_BEGIN_TAG) && current.endsWith(L_END_TAG)) {
                        table.addEntry(current.substring(current.indexOf(L_BEGIN_TAG) + 1,
                                current.indexOf(L_END_TAG)).trim(), lineNum + 1);
                        continue;
                    }
                    sequence.put(++lineNum, current);
                }
            }

            totalNumber = sequence.size();

            String absolutePath;
            absolutePath = file.getAbsolutePath();

            filePath = absolutePath.substring(0, absolutePath.indexOf(name));

            if (fileReader != null) {
                fileReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void advance() {
        currInsNumber++;
        currInstruct = sequence.get(currInsNumber);
        currCmd = commandType();
    }

    public boolean hasMoreCommands() {
        return (totalNumber - currInsNumber - 1) > 0;
    }

    private static boolean isNumeric(final String str) {
        Pattern pattern = Pattern.compile(NUMBERS);
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public String symbol() {
        if (currCmd.compareTo(CommandType.C_COMMAND) == 0)
            throw new RuntimeException("Kein A_COMMAND oder C_COMMAND Typ!");

        if (currCmd.compareTo(CommandType.A_COMMAND) == 0) return currInstruct.substring(1);

        if (currCmd.compareTo(CommandType.L_COMMAND) == 0)
            return currInstruct.substring(1, currInstruct.indexOf(L_END_TAG));

        return null;
    }

    public CommandType commandType() {
        if (currInstruct.startsWith(A_TAG)) return CommandType.A_COMMAND;

        if (currInstruct.startsWith(L_BEGIN_TAG) && currInstruct.endsWith(L_END_TAG)) return CommandType.L_COMMAND;

        return CommandType.C_COMMAND;
    }

    public String comp() {
        String comp;
        if (currCmd.compareTo(CommandType.C_COMMAND) == 0) {
            int jumpIdx = currInstruct.indexOf(JUMP_SEPARATOR);
            int destIdx = currInstruct.indexOf(DEST_SEPARATOR);
            int start = destIdx > 0 ? destIdx + 1 : 0;
            int end = jumpIdx > 0 ? jumpIdx : currInstruct.length();
            comp = currInstruct.substring(start, end).trim().toUpperCase();
            if (comp.length() < 1) {
                throw new RuntimeException("Syntax Fehler!");
            }
            return comp;
        } else {
            throw new RuntimeException("Kein C_COMMAND Typ!");
        }
    }

    public String jump() {
        if (currCmd.compareTo(CommandType.C_COMMAND) == 0) {
            int idx = currInstruct.indexOf(JUMP_SEPARATOR);
            if (idx > 0) {
                return currInstruct.substring(idx + 1).trim().toUpperCase();
            } else {
                return "null";
            }
        } else {
            throw new RuntimeException("Kein C_COMMAND Typ!");
        }
    }

    public String dest() {
        if (currCmd.compareTo(CommandType.C_COMMAND) == 0) {
            int idx = currInstruct.indexOf(DEST_SEPARATOR);
            return idx > 0 ? currInstruct.substring(0, idx).trim().toUpperCase() : "null";
        } else {
            throw new RuntimeException("Kein C_COMMAND Typ!");
        }
    }

    public ArrayList<String> parse() {
        ArrayList<String> parsed = new ArrayList<>();
        String symbol;
        int address = -1;
        int varAddress = 0x0F;
        StringBuilder stringBuilder = new StringBuilder();

        while (hasMoreCommands()) {
            advance();
            if (currCmd.compareTo(CommandType.A_COMMAND) == 0) {
                symbol = symbol();
                if (isNumeric(symbol)) {
                    address = Integer.parseInt(symbol);
                } else {
                    if (table.contains(symbol)) {
                        address = table.getAddress(symbol);
                    } else {
                        address = ++varAddress;
                        table.addEntry(symbol, address);
                    }
                }

                parsed.add(code.getATypeBinary(address) + LINE_SEPARATOR);
            }

            if (currCmd.compareTo(CommandType.C_COMMAND) == 0) {
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append(Code.PREFIX)
                        .append(code.comp(comp()))
                        .append(code.dest(dest()))
                        .append(code.jump(jump()));
                parsed.add(stringBuilder + LINE_SEPARATOR);
            }
        }
        return parsed;
    }

    public void compile() {
        ArrayList<String> compiled = parse();
        File dest = new File(filePath + File.separator + fileName + DEST);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(dest);
            for (String current : compiled) {
                fileWriter.write(current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
