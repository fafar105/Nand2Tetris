public class VMToHackTranslator {
    public static void main(String[] args) {
        Parser p = new Parser(args[0]);
        HackOps cW = new HackOps(args[0]);
        while (p.hasMoreCommands()) {
            p.goForward();
            CmdType cmdType = p.commandType();
            if (cmdType == CmdType.CMD_ARITHMETIC) {
                cW.writeArithmetic(p.arg1());
            } else if (cmdType == CmdType.CMD_PUSH) {
                cW.writePushPop(CmdType.CMD_PUSH, p.arg1(), p.arg2());
            } else if (cmdType == CmdType.CMD_POP) {
                cW.writePushPop(CmdType.CMD_POP, p.arg1(), p.arg2());
            } else {
                return;
            }
        }
        cW.closeFile();
    }
}

