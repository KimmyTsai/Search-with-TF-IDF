import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TFIDFSearch {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        String inputPath = args[0];
        String testcase = args[1];
        String outputPath = "output.txt";

        DocsProcessor doc = new DocsProcessor();
        List<String> textBlocks = doc.deserializeTextFile(inputPath);
        List<Trie> textParts = convertTextBlocksToTries(textBlocks);
        
        BufferedReader br = new BufferedReader(new FileReader(testcase));
        List<List<String>> categorizedWords = new ArrayList<>();
        List<String> cutter = new ArrayList<>();
        int number = Integer.parseInt(br.readLine().trim());

        String line;
        while ((line = br.readLine()) != null) {
            List<String> words;
            if (line.contains(" AND ")) {
                words = Arrays.asList(line.split(" AND "));
                cutter.add("AND");
            } else if (line.contains(" OR ")) {
                words = Arrays.asList(line.split(" OR "));
                cutter.add("OR");
            } else {
                words = Collections.singletonList(line);
                cutter.add("S");
            }
            categorizedWords.add(words);
        }
        br.close();

        Calculator cal = new Calculator();
        BufferedWriter fw = new BufferedWriter(new FileWriter(outputPath));

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<Integer>>> futures = new ArrayList<>();

        for (int i = 0; i < categorizedWords.size(); i++) {
            List<String> queryWords = categorizedWords.get(i);
            String operation = cutter.get(i);
            futures.add(executor.submit(() -> {
                List<Integer> resultDocs;
                if (operation.equals("AND")) {
                    resultDocs = findDocumentsContainingAllWords(doc, textParts, queryWords);
                } else if (operation.equals("OR")) {
                    resultDocs = findDocumentsContainingAnyWord(doc, textParts, queryWords);
                } else {
                    resultDocs = doc.findDocumentsContainingWord(textParts, queryWords.get(0));
                }

                List<DocumentScore> documentScores = new ArrayList<>();
                for (int docIndex : resultDocs) {
                    double tfidfSum = 0;
                    for (String word : queryWords) {
                        tfidfSum += cal.tfIdfCalculate(word, docIndex, textParts);
                    }
                    documentScores.add(new DocumentScore(docIndex, tfidfSum));
                }

                Collections.sort(documentScores, new Comparator<DocumentScore>() {
                    @Override
                    public int compare(DocumentScore d1, DocumentScore d2) {
                        if (d1.tfidf == d2.tfidf) {
                            return Integer.compare(d1.docIndex, d2.docIndex);
                        }
                        return Double.compare(d2.tfidf, d1.tfidf);
                    }
                });

                List<Integer> output = new ArrayList<>();
                for (int j = 0; j < number; j++) {
                    if (j < documentScores.size()) {
                        output.add(documentScores.get(j).docIndex);
                    } else {
                        output.add(-1);
                    }
                }
                return output;
            }));
        }

        for (Future<List<Integer>> future : futures) {
            List<Integer> result = future.get();
            for (int docId : result) {
                fw.write(docId + " ");
            }
            fw.write("\n");
        }

        executor.shutdown();
        fw.close();
    }

    public static List<Integer> findDocumentsContainingAllWords(DocsProcessor doc, List<Trie> textParts, List<String> words) {
        List<Integer> resultDocs = doc.findDocumentsContainingWord(textParts, words.get(0));
        for (int i = 1; i < words.size(); i++) {
            resultDocs.retainAll(doc.findDocumentsContainingWord(textParts, words.get(i)));
        }
        return resultDocs;
    }

    public static List<Integer> findDocumentsContainingAnyWord(DocsProcessor doc, List<Trie> textParts, List<String> words) {
        Set<Integer> resultDocsSet = new HashSet<>();
        for (String word : words) {
            resultDocsSet.addAll(doc.findDocumentsContainingWord(textParts, word));
        }
        return new ArrayList<>(resultDocsSet);
    }

    public static List<Trie> convertTextBlocksToTries(List<String> textBlocks) {
        List<Trie> tries = new ArrayList<>();
        int docIndex = 0;
        for (String block : textBlocks) {
            Trie trie = new Trie();
            trie.insertLine(block, docIndex);
            tries.add(trie);
            docIndex++;
        }
        return tries;
    }
}

class DocumentScore {
    int docIndex;
    double tfidf;

    DocumentScore(int docIndex, double tfidf) {
        this.docIndex = docIndex;
        this.tfidf = tfidf;
    }
}

class Calculator {
    private CacheTrie cacheTrie;

    public Calculator() {
        cacheTrie = new CacheTrie();
    }

    public double tf(String term, Trie trie) {
        int number_term_in_doc = trie.searchWordOccurrences(term);
        int wordCount = trie.getTotalWordCount();

        return (double) number_term_in_doc / wordCount;
    }

    public double idf(String term, List<Trie> docs) {
        Double cachedValue = cacheTrie.search(term);
        if (cachedValue != null) {
            return cachedValue;
        }

        double count = 0;
        for (Trie trie : docs) {
            if (trie.searchWordOccurrences(term) > 0) {
                count++;
            }
        }
        double idfValue = Math.log((double) docs.size() / (count == 0 ? 1 : count));
        cacheTrie.insert(term, idfValue);

        return idfValue;
    }

    public double tfIdfCalculate(String term, int number, List<Trie> docs) {
        return tf(term, docs.get(number)) * idf(term, docs);
    }
}

class DocsProcessor {
    @SuppressWarnings("unchecked")
    public List<String> deserializeTextFile(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<String>) ois.readObject();
        }
    }

    public List<Integer> findDocumentsContainingWord(List<Trie> tries, String word) {
        List<Integer> docIndices = new ArrayList<>();
        for (int i = 0; i < tries.size(); i++) {
            if (tries.get(i).searchWordOccurrences(word) > 0) {
                docIndices.add(i);
            }
        }
        return docIndices;
    }
}

class TrieNode implements Serializable {
    TrieNode[] children;
    int wordCount;
    double idfValue;
    boolean isEndOfWord;
    List<Integer> docIndices;

    public TrieNode() {
        this.children = new TrieNode[26];
        this.wordCount = 0;
        this.idfValue = -1;
        this.isEndOfWord = false;
        this.docIndices = new ArrayList<>();
    }
}

class Trie implements Serializable {
    private TrieNode root;
    private int totalWordCount;

    public Trie() {
        root = new TrieNode();
        totalWordCount = 0;
    }

    public void insertLine(String line, int docIndex) {
        for (String word : line.split(" ")) {
            if (!word.isEmpty()) {
                insert(word, docIndex);
            }
        }
    }

    public void insert(String word, int docIndex) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                continue;
            }
            int index = ch - 'a';
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        if (!current.docIndices.contains(docIndex)) {
            current.docIndices.add(docIndex);
        }
        current.isEndOfWord = true;
        current.wordCount++;
        totalWordCount++;
    }

    public int searchWordOccurrences(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                return 0;
            }
            int index = ch - 'a';
            if (current.children[index] == null) {
                return 0;
            }
            current = current.children[index];
        }
        return current.isEndOfWord ? current.wordCount : 0;
    }

    public List<Integer> searchWordInDocuments(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                return new ArrayList<>();
            }
            int index = ch - 'a';
            if (current.children[index] == null) {
                return new ArrayList<>();
            }
            current = current.children[index];
        }
        return current.isEndOfWord ? current.docIndices : new ArrayList<>();
    }

    public int getTotalWordCount() {
        return totalWordCount;
    }
}

class CacheTrie {
    private TrieNode root;

    public CacheTrie() {
        root = new TrieNode();
    }

    public void insert(String term, double idfValue) {
        TrieNode current = root;
        for (char ch : term.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                continue;
            }
            int index = ch - 'a';
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        current.isEndOfWord = true;
        current.idfValue = idfValue;
    }

    public Double search(String term) {
        TrieNode current = root;
        for (char ch : term.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                return null;
            }
            int index = ch - 'a';
            if (current.children[index] == null) {
                return null;
            }
            current = current.children[index];
        }
        return current.isEndOfWord ? current.idfValue : null;
    }
}
