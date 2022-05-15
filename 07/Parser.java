import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private String current;
    private int ptr;
    private List<String> cmds;
    private List<String> arithmetics;

    public Parser(String filePath) {
        String line;
        initArithmetics();
        initPtr();
        cmds = new ArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            line = in.readLine();
            while (line != null) {
                if (line.equals("") || line.charAt(0) == '/' && line.charAt(1) == '/') {
                    line = in.readLine();
                    continue;
                }
                String[] cmdSplitter = line.split("//");
                cmds.add(cmdSplitter[0]);
                line = in.readLine();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goForward() {
        ptr++;
        this.current = cmds.get(ptr);
    }

    public String arg1() {
        return commandType() == CmdType.CMD_ARITHMETIC ? current : current.split(" ")[1];
    }

    public int arg2() {
        return Integer.parseInt(current.split(" ")[2]);
    }

    private void initArithmetics() {
        arithmetics = Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
    }

    public Boolean hasMoreCommands() {
        return ptr < cmds.size() - 1;
    }

    public CmdType commandType() {
        if (arithmetics.contains(current)) return CmdType.CMD_ARITHMETIC;
        else if (current.startsWith("push")) return CmdType.CMD_PUSH;
        else if (current.startsWith("pop")) return CmdType.CMD_POP;
        return null;
    }

    public void initPtr() {
        ptr = -1;
    }
}
