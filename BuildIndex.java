import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class BuildIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static void main(String[] args) {
        String inputFilePath = args[0];
        String outputFilePath = "processed_docs.txt";
        String serializedFilePath = inputFilePath.substring(inputFilePath.lastIndexOf('/')+1, inputFilePath.lastIndexOf("."));
        
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(inputFilePath));
            List<String> sentences = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                String processedLine = processText(line);
                for (String sentence : splitIntoSentences(processedLine)) {
                    if (!sentence.trim().isEmpty()) {
                        sentences.add(sentence.trim());
                    }
                }
            }
            
            reader.close();
            
            List<String> textBlocks = splitSentencesIntoTextBlocks(sentences, 5);
            
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath));
            for (String block : textBlocks) {
                writer.write(block);
                writer.newLine();
            }
            
            writer.close();
            
            serializeTextBlocks(textBlocks, serializedFilePath);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String processText(String text) {
        return text.toLowerCase()
                   .replaceAll("[^a-z\\s]", " ")
                   .replaceAll("\\s+", " ")      
                   .trim();
    }

    private static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        for (String sentence : text.split("\\.\\s*")) {
            if (!sentence.trim().isEmpty()) {
                sentences.add(sentence.trim());
            }
        }
        return sentences;
    }
    
    private static List<String> splitSentencesIntoTextBlocks(List<String> sentences, int blockSize) {
        List<String> textBlocks = new ArrayList<>();
        StringBuilder currentBlock = new StringBuilder();
        int count = 0;
        
        for (String sentence : sentences) {
            if (count > 0) {
                currentBlock.append(" ");
            }
            currentBlock.append(sentence);
            count++;
            
            if (count == blockSize) {
                textBlocks.add(currentBlock.toString());
                currentBlock.setLength(0); 
                count = 0;
            }
        }
        
        if (count > 0) {
            textBlocks.add(currentBlock.toString());
        }
        
        return textBlocks;
    }

    private static void serializeTextBlocks(List<String> textBlocks, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(textBlocks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
