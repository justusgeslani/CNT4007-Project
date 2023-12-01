import java.io.*;
import java.util.*;

public class CommonConfig {
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public void loadCommonFile() {
        try (FileReader file = new FileReader("Common.cfg");
             Scanner fileReader = new Scanner(file)) {

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                String[] temp = line.split(" ");

                switch (temp[0]) {
                    case "NumberOfPreferredNeighbors":
                        numberOfPreferredNeighbors = Integer.parseInt(temp[1]);
                        break;
                    case "UnchokingInterval":
                        unchokingInterval = Integer.parseInt(temp[1]);
                        break;
                    case "OptimisticUnchokingInterval":
                        optimisticUnchokingInterval = Integer.parseInt(temp[1]);
                        break;
                    case "FileName":
                        fileName = temp[1];
                        break;
                    case "FileSize":
                        fileSize = Integer.parseInt(temp[1]);
                        break;
                    case "PieceSize":
                        pieceSize = Integer.parseInt(temp[1]);
                        break;
                    default:
                        break;
                }
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println("Error: Config file not found.");
        }
        catch (NumberFormatException ex) {
            System.out.println("Error: Invalid number format in config file.");
        }
        catch (IOException ex) {
            System.out.println("Error reading the file: " + ex.getMessage());
        }
    }

    public class ConfigDTO {
        private final int numberOfPreferredNeighbors;
        private final int unchokingInterval;
        private final int optimisticUnchokingInterval;
        private final String fileName;
        private final int fileSize;
        private final int pieceSize;
        private ConfigDTO() {
            this.numberOfPreferredNeighbors = CommonConfig.this.numberOfPreferredNeighbors;
            this.unchokingInterval = CommonConfig.this.unchokingInterval;
            this.optimisticUnchokingInterval = CommonConfig.this.optimisticUnchokingInterval;
            this.fileName = CommonConfig.this.fileName;
            this.fileSize = CommonConfig.this.fileSize;
            this.pieceSize = CommonConfig.this.pieceSize;
        }
        public int getNumberOfPreferredNeighbors() { return numberOfPreferredNeighbors; }
        public int getUnchokingInterval() { return unchokingInterval; }
        public int getOptimisticUnchokingInterval() { return optimisticUnchokingInterval; }
        public String getFileName() { return fileName; }
        public int getFileSize() { return fileSize; }
        public int getPieceSize() { return pieceSize; }
    }
    public ConfigDTO createConfigDTO() {
        return new ConfigDTO();
    }
}