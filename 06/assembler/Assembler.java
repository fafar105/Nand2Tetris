public class Assembler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bitte geben Sie ein: Assembler <file>");
            System.exit(1);
        }
        SymbolTable t = new SymbolTable();
        Code c = new Code();
        Parser p = new Parser(args[0], t, c);
        p.compile();
    }
}