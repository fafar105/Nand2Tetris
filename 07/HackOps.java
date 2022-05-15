import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HackOps {
    private String fileName;
    private int lblFlag;
    private BufferedWriter bW;

    public HackOps(String outputPath) {
        lblFlag = 0;
        File file = new File(outputPath.replace(".vm", ".asm"));
        String tempFileName = file.getName();
        fileName = tempFileName.substring(0, tempFileName.lastIndexOf('.'));
        try {
            bW = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArithmetic(String command) {
        try {
            bW.newLine();
            switch (command) {
                case "add" -> {
                    bW.write("//add");
                    bW.newLine();
                    binOp1("+");
                }
                case "sub" -> {
                    bW.write("//sub");
                    bW.newLine();
                    binOp1("-");
                }
                case "neg" -> {
                    bW.write("//neg");
                    bW.newLine();
                    negOp();
                }
                case "eq" -> {
                    bW.write("//eq");
                    bW.newLine();
                    binOp1("-");
                    binOp2("=");
                }
                case "gt" -> {
                    bW.write("//gt");
                    bW.newLine();
                    binOp1("-");
                    binOp2(">");
                }
                case "lt" -> {
                    bW.write("//lt");
                    bW.newLine();
                    binOp1("-");
                    binOp2("<");
                }
                case "and" -> {
                    bW.write("//and");
                    bW.newLine();
                    binOp1("&");
                }
                case "or" -> {
                    bW.write("//or");
                    bW.newLine();
                    binOp1("|");
                }
                case "not" -> {
                    bW.write("//not");
                    bW.newLine();
                    noOp();
                }
                default -> throw new RuntimeException("UnExcepted arithmetic");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void writePushPop(CmdType cmdType, String segment, int index) {
        try {
            bW.newLine();
            if (cmdType == CmdType.CMD_PUSH) {
                bW.write("//push " + segment + " " + index);
                bW.newLine();
                switch (segment) {
                    case "constant" -> pushConstant(index);
                    case "local" -> pushLATT("LCL", index);
                    case "argument" -> pushLATT("ARG", index);
                    case "this" -> pushLATT("THIS", index);
                    case "that" -> pushLATT("THAT", index);
                    case "temp" -> pushTmp(index);
                    case "pointer" -> pushPtr(index);
                    case "static" -> pushStatic(index);
                }
            } else if (cmdType == CmdType.CMD_POP) {
                bW.write("//pop " + segment + " " + index);
                bW.newLine();
                switch (segment) {
                    case "local" -> popLATT("LCL", index);
                    case "argument" -> popLATT("ARG", index);
                    case "this" -> popLATT("THIS", index);
                    case "that" -> popLATT("THAT", index);
                    case "temp" -> popTmp(index);
                    case "pointer" -> popPtr(index);
                    case "static" -> popStatic(index);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void noOp() {
        try {
            SPMinusMinus();
            AP();
            bW.write("M=!M");
            bW.newLine();
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void negOp() {

        try {
            noOp();
            SPMinusMinus();
            AP();
            bW.write("M=M+1");
            bW.newLine();
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binOp1(String arithmetic) {
        try {
            SPMinusMinus();
            DEAP();
            SPMinusMinus();
            bW.write("A=M");
            bW.newLine();
            bW.write("M=M" + arithmetic + "D");
            bW.newLine();
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binOp2(String arithmetic) {
        try {
            int a = 0, b = 0, c = 0;
            switch (arithmetic) {
                case "=" -> a = -1;
                case ">" -> b = -1;
                case "<" -> c = -1;
            }
            String flag = "LABELFLAG_";
            String lbl1 = flag + lblFlag++;
            String lbl2 = flag + lblFlag++;
            String lbl3 = flag + lblFlag++;
            String lblEnd = flag + lblFlag++;
            SPMinusMinus();
            DEAP();
            bW.write("@" + lbl1);
            bW.newLine();
            bW.write("D;JEQ");
            bW.newLine();
            bW.write("@" + lbl2);
            bW.newLine();
            bW.write("D;JGT");
            bW.newLine();
            bW.write("@" + lbl3);
            bW.newLine();
            bW.write("D;JLT");
            bW.newLine();
            setLabels(a, lbl1, lbl2, lblEnd);
            AP();
            bW.write("M=" + b);
            bW.newLine();
            JMP(lblEnd);
            setLabels(c, lbl3, lblEnd, lblEnd);
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLabels(int a, String lbl1, String lbl2, String lblEnd) throws IOException {
        bW.write("(" + lbl1 + ")");
        bW.newLine();
        AP();
        bW.write("M=" + a);
        bW.newLine();
        JMP(lblEnd);
        bW.write("(" + lbl2 + ")");
        bW.newLine();
    }


    private void AP() {
        try {
            bW.write("@" + "SP");
            bW.newLine();
            bW.write("A=M");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void JMP(String lbl) {
        try {
            bW.write("@" + lbl);
            bW.newLine();
            bW.write("0;JMP");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void DEAP() {
        try {
            bW.write("@" + "SP");
            bW.newLine();
            bW.write("A=M");
            bW.newLine();
            bW.write("D=M");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void DEAPPlusI(String pointer, int index) {
        try {
            bW.write("@" + pointer);
            bW.newLine();
            bW.write("D=M");
            bW.newLine();
            bW.write("@" + index);
            bW.newLine();
            bW.write("A=D+A");
            bW.newLine();
            bW.write("D=M");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void APED(String pointer) {
        try {
            bW.write("@" + pointer);
            bW.newLine();
            bW.write("A=M");
            bW.newLine();
            bW.write("M=D");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void DEX(int x) {
        DEX(String.valueOf(x));
    }

    private void DEX(String x) {
        try {
            bW.write("@" + x);
            bW.newLine();
            bW.write("D=A");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MED(String x) {
        try {
            bW.write("@" + x);
            bW.newLine();
            bW.write("M=D");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DEM(String addr) {
        try {
            bW.write("@" + addr);
            bW.newLine();
            bW.write("D=M");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void SPPlusPlus() {
        try {
            bW.write("@SP");
            bW.newLine();
            bW.write("M=M+1");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void SPMinusMinus() {
        try {
            bW.write("@SP");
            bW.newLine();
            bW.write("M=M-1");
            bW.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushConstant(int index) {
        DEX(index);
        APED("SP");
        SPPlusPlus();
    }

    private void pushLATT(String pointer, int index) {
        DEAPPlusI(pointer, index);
        APED("SP");
        SPPlusPlus();
    }

    private void pushTmp(int index) {
        DEM("R" + (index + 5));
        APED("SP");
        SPPlusPlus();
    }

    private void pushPtr(int index) {
        if (index == 0) {
            DEM("THIS");
        } else {
            DEM("THAT");
        }
        APED("SP");
        SPPlusPlus();
    }

    private void pushStatic(int index) {
        DEM(fileName + "." + index);
        APED("SP");
        SPPlusPlus();
    }

    private void popTmp(int index) {
        SPMinusMinus();
        DEAP();
        MED("R" + (5 + index));
    }

    private void popPtr(int index) {
        SPMinusMinus();
        DEAP();
        if (index == 0) {
            MED("THIS");
        } else {
            MED("THAT");
        }
    }

    private void popLATT(String pointer, int index) {
        try {
            bW.write("@" + pointer);
            bW.newLine();
            bW.write("D=M");
            bW.newLine();
            bW.write("@" + index);
            bW.newLine();
            bW.write("D=D+A");
            bW.newLine();
            bW.write("@addr");
            bW.newLine();
            bW.write("M=D");
            bW.newLine();
            SPMinusMinus();
            DEAP();
            APED("addr");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void popStatic(int index) {
        SPMinusMinus();
        DEAP();
        MED(fileName + "." + index);
    }


    public void closeFile() {
        try {
            bW.newLine();
            bW.write("//end");
            bW.newLine();
            bW.write("(END)");
            bW.newLine();
            bW.write("@END");
            bW.newLine();
            bW.write("0;JMP");
            bW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
