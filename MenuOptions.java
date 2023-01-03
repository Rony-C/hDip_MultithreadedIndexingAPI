package ie.atu.sw;
public class MenuOptions implements  MenuOptionsInterface{

    /**
     * Displays menu content and options to the user.
     *
     */
    public static void show() {
        System.out.println(ConsoleColour.WHITE);
        System.out.println("************************************************************");
        System.out.println("*       ATU - Dept. Computer Science & Applied Physics     *");
        System.out.println("*                                                          *");
        System.out.println("*              Virtual Threaded Text Indexer               *");
        System.out.println("*                                                          *");
        System.out.println("************************************************************");
        System.out.println("(1) Specify Text File");
        System.out.println("(2) Configure Dictionary");
        System.out.println("(3) Configure Common Words");
        System.out.println("(4) Specify Output File");
        System.out.println("(5) Execute");
        System.out.println("(6) Quit");

        System.out.print(ConsoleColour.BLACK_BOLD_BRIGHT);
        System.out.print("Select Option [1-6]>");
        System.out.println();
    }
}
