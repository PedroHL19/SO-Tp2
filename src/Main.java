public class Main {
    public static void main(String[] args) {
        ReadAndWriteTxt reader = new ReadAndWriteTxt();
        // for (int i = 1; i <= 10; i++) {
        //     if (i == 10) {
        //         reader.txtReader("src\\assets\\TESTE-10.txt");
        //     } else {
        //         reader.txtReader("src\\assets\\TESTE-0" + i + ".txt");
        //     }

        // }
        reader.txtReader("src\\assets\\TESTE-02.txt");
        //reader.txtReader("src\\assets\\TESTE-01.txt");
    }
}