import java.io.*;
import java.util.ArrayList;

public class ReadAndWriteTxt {
    private ArrayList<Configuration> accessSequence;
    private Configuration config;
    private Calculations calc;

    public void txtReader(String filePath) {
        config = new Configuration();
        accessSequence = new ArrayList<>();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            int lineNum = 0;
            
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lineNum++;
                    processLine(line.trim(), lineNum);
                }
            }
            reader.close();
            
            calc = new Calculations();
            calc.setConfig(config, accessSequence);
            writeOutput(filePath);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line, int lineNum) {
        String[] parts = line.trim().split("\\s+");
        
        if (lineNum == 1) {
            config.setTotalPages(Integer.parseInt(parts[0]));
        } else if (lineNum == 2) {
            config.setTotalFrames(Integer.parseInt(parts[0]));
        } else if (lineNum == 3) {
            config.setClockInterval(Integer.parseInt(parts[0]));
        } else {
            Configuration access = new Configuration();
            access.setPageNumber(Integer.parseInt(parts[0]));
            access.setAccessTime(Integer.parseInt(parts[1]));
            access.setWrite(parts[2].equals("W"));
            accessSequence.add(access);
        }
    }

    public void writeOutput(String inputFilePath) {
        try {
            File inputFile = new File(inputFilePath);
            String outputFileName = inputFile.getName().replace(".txt", "-RESULTADO.txt");
            String outputFilePath = inputFile.getParent() + File.separator + outputFileName;

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            calc.write(writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}