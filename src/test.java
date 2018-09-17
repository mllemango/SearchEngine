import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class test {

    public static void main(String[] args) throws IOException {
        //get raw file
        String raw;
        String folderName = "C:\\Users\\melwa\\Documents\\Melly Items\\University Items\\3A\\MSCI 541\\Extracted3\\" + "020990";
        String fileName = "LA020990-0182";

        FileReader rawReader = new FileReader(folderName + "\\"+ fileName + ".txt");
        BufferedReader rawBuffer = new BufferedReader(rawReader);


        List<String> sentences = new ArrayList<>();
        String QBSummary = "";
        while((raw = rawBuffer.readLine())!= null) {
            if (raw.contains("<TEXT>") || raw.contains("<GRAPHIC>")) {
                while (!raw.contains("</TEXT>") || !raw.contains("</GRAPHIC>")) {
                //while (!(raw = rawBuffer.readLine()).contains("</TEXT>") || !(raw = rawBuffer.readLine()).contains("</GRAPHIC>")) {

                    raw = rawBuffer.readLine();
                    if(raw.contains("</TEXT>") || raw.contains("</GRAPHIC>")){
                        break;
                    }
                    //System.out.println(raw);
                    if (raw.contains("<P>")) {
                        String sentence = "";
                        while (!(raw = rawBuffer.readLine()).contains("</P>")) {
                            sentence = sentence + raw;
                            //System.out.println(raw);
                        }
                        //break sentence apart on punctuation
                        //merge sentence if both are less than 350 char
                        //find score of each sentence in the array
                        //return highest scoring sentence as summary

                        String[] splitSentence = sentence.split("(?<=[.?!])");
                        sentences.addAll(Arrays.asList(splitSentence));

                    }
                }
            }
        }
        rawBuffer.close();

        int i = 0;
        while(i < sentences.size()-1){
            if (sentences.get(i).length()<350){
                if(350 - sentences.get(i).length() > sentences.get(i+1).length()){
                    System.out.println("sentence 1: " + sentences.get(i));
                    System.out.println("sentence 2: " + sentences.get(i+1));
                    sentences.set(i, sentences.get(i) + sentences.get(i+1));
                    System.out.println("new merge: " + sentences.get(i));
                    System.out.println();
                    sentences = condenseArray(i+1, sentences);
                    i--;
                }
            }
            i++;
        }


        for(String t: sentences){
            System.out.println(t + " char count: " + t.length());
        }
    }

    public static String getInput(){
        Scanner reader = new Scanner(System.in);
        System.out.println("Please enter search term(s)");

        String input = reader.nextLine();
        if(input.isEmpty()){
            System.out.println("You did not enter anything, please try again");
            input = getInput();
        }
        reader.close();
        return input;

    }

    public static rankingObjects readingTxtFiles() throws IOException {
        System.out.println("reading text files");
        //String[] pathInfo = inputReader();
        String index_path = "C:\\Users\\melwa\\Documents\\Melly Items\\University Items\\3A\\MSCI 541\\Extracted3\\metadata.txt";

        //read in metadata file
        FileReader lexiconReader = new FileReader(index_path);
        BufferedReader lexiconBuffer = new BufferedReader(lexiconReader);

        String metaLine;
        lexicon lexi = new lexicon();
        invIndex invIndex = new invIndex();
        boolean lexiconStart = false;
        boolean invIndexStart = false;
        Map<Integer, metadata> id_data = new HashMap<Integer, metadata>();
        Integer internal_id;
        String meta_docno;
        String meta_date;
        String meta_headline;
        Integer meta_wordCount;
        int semicolon1;
        int semicolon2;
        int semicolon3;
        int semicolon4;

        while((metaLine = lexiconBuffer.readLine()) != null){
            //starts adding into lexicon when file says Lexicon
            if(metaLine.contains("Lexicon")){
                lexiconStart = true;
            }
            //stops adding into lexicon when reached inverted index
            if(metaLine.contains("Inverted Index")){
                lexiconStart = false;
                invIndexStart = true;
            }

            if (!lexiconStart && !invIndexStart){
                //adding metadata
                semicolon1 = metaLine.indexOf(";");
                semicolon2 = metaLine.indexOf(";", semicolon1 + 1);
                semicolon3 = metaLine.indexOf(";", semicolon2 + 1);
                semicolon4 = metaLine.indexOf("~", semicolon3+1);

                internal_id = Integer.parseInt(metaLine.substring(0,semicolon1));
                meta_docno = metaLine.substring(semicolon1+1, semicolon2);
                meta_date = metaLine.substring(semicolon2+1, semicolon3);
                meta_headline = metaLine.substring(semicolon3+1, semicolon4);
                meta_wordCount = Integer.parseInt(metaLine.substring(semicolon4+1, metaLine.length()));

                metadata temp = new metadata(meta_docno, meta_date, meta_headline);
                temp.setWordCount(meta_wordCount);
                id_data.put(internal_id, temp);
            }
            else if(lexiconStart){
                //adding lexicon
                if(metaLine.contains("Lexicon")){
                    continue;
                }
                String term = metaLine.substring(metaLine.indexOf(";")+1,metaLine.length());
                lexi.addTerm(term);
            }
            else if(invIndexStart){
                //adding inverted index
                if(metaLine.contains("Inverted")){
                    continue;
                }
                Integer termID = Integer.parseInt(metaLine.substring(0, metaLine.indexOf(":")));
                Integer docID;
                Integer wordCount;

                metaLine = metaLine.substring(metaLine.indexOf(":")+1, metaLine.length());

                String[] split = metaLine.split(";");
                int i = 0;
                while (i < split.length){
                    docID = Integer.parseInt(split[i]);
                    wordCount = Integer.parseInt(split[i+1]);
                    invIndex.addPosting(termID, wordCount, docID);
                    i = i+2;
                }
            }
        }

        lexiconBuffer.close();

        rankingObjects newCollection = new rankingObjects(id_data, lexi,invIndex);
        return newCollection;

    }

    public static String[] inputReader(){
        String[] pathInfo = new String[3];

        //asking user for gz file location
        Scanner reader = new Scanner(System.in);
        System.out.println("Please enter location of the index");

        String index_path = reader.nextLine();

        //check if entered line is empty
        if (index_path.isEmpty()){
            System.out.println("Nothing was entered! Please try again");
            System.exit(0);
        }

        pathInfo[0] = index_path;

        //check if file exists
        File f = new File(index_path);
        if (f.exists()== false){
            System.out.println("File does not exist");
            System.exit(0);
        }

        System.out.println("Please enter location of the query file");

        String queryFile = reader.nextLine();

        //check if entered line is empty
        if (queryFile.isEmpty()){
            System.out.println("Nothing was entered! Please try again");
            System.exit(0);
        }

        pathInfo[1] = queryFile;

        //check if file exists
        File f2 = new File(queryFile);
        if (f2.exists()== false){
            System.out.println("File does not exist");
            System.exit(0);
        }

        System.out.println("Please enter the name for the output file");

        String output = reader.nextLine();
        pathInfo[2] = output;

        reader.close();

        return pathInfo;
    }

    public static ArrayList<String> tokenize(String text){
        text = text.toLowerCase();
        ArrayList<String> tokens = new ArrayList();
        int start = 0;
        int i = 0;
        while (i<text.length()){
            char c = text.charAt(i);
            if(!Character.isLetterOrDigit(c)){
                if(start!=i){
                    String token = text.substring(start, i);
                    //tokens.add(PorterStemmer.stem(token));
                    tokens.add(token);
                }
                start = i+1;
            }
            i++;
        }
        if(start!=i){
            String token = text.substring(start, i);
            //tokens.add(PorterStemmer.stem(token));
            tokens.add(token);
        }

        return tokens;
    }

    public static ArrayList<Integer> transformtoTokenID(ArrayList<String> tokens, lexicon lexi){
        ArrayList<Integer> tokenIDs = new ArrayList<>();
        for (int i = 0; i<tokens.size(); i++){
            tokenIDs.add(lexi.getID(tokens.get(i)));
        }
        return tokenIDs;

    }

    public static ArrayList<Integer> BM25(rankingObjects objs, ArrayList<Integer> tokenIDs){
        Map<Integer, metadata> id_data = objs.getId_data();
        invIndex invIndex = objs.getInvIndex();
        //PriorityQueue<Integer> rankedDocIDs=new PriorityQueue<Integer>();
        ArrayList<Integer> rankedDocIDs = new ArrayList<>();

        //executing BM25
        double docCount = id_data.size();
        //calculating avgDocLength
        double totDocLengths = 0;
        for (Map.Entry<Integer, metadata> docentry : id_data.entrySet()){
            double docLength = docentry.getValue().getWordCount();
            totDocLengths = totDocLengths + docLength;
        }
        double avgDocLength = totDocLengths/docCount;
        double k1 = 1.2;
        double k2 = 7;
        double b = 0.75;

        //for each term i in query, calculate weight for each doc j
        //add up each weight for term i for doc j
        //store score in hashtable<docId, score>

        //term at a time scoring:
        //for each term, get posting list
        //calculate score for each doc in posting list
        HashMap<Integer, Double> docID2score = new HashMap<>();

        for (int i =0; i<tokenIDs.size(); i++){
            Integer termID = tokenIDs.get(i);
            ArrayList<posting> termiPostings = invIndex.getPostings(termID);
            for(int j = 0; j<termiPostings.size(); j++) {
                int docID = termiPostings.get(j).getDocID();
                double docLength = id_data.get(docID).getWordCount();
                double k = k1 * ((1 - b) + b * docLength / avgDocLength);
                double tf = termiPostings.get(j).getCount();
                double TFinDoc = (k1 + 1) * tf / (k + tf);
                double TFinQuery = (k2 + 1) * 1 / (k2 + 1);
                double idf = Math.log((docCount - termiPostings.size() + 0.5) / (termiPostings.size() + 0.5));

                double BM = TFinDoc * TFinQuery * idf;

                if (docID2score.containsKey(docID)) {
                    double BMSum = BM + docID2score.get(docID);
                    docID2score.put(docID, BMSum);

                } else {
                    docID2score.put(docID, BM);
                }
            }
        }
        //getting top 1000
        Map<Integer, Double> docID2ScoreSorted = sortByComparator(docID2score, false);

        int i = 0;

        Iterator<Map.Entry<Integer, Double>> it = docID2ScoreSorted.entrySet().iterator();
        while (it.hasNext() && i<10) {
            Map.Entry<Integer, Double> pair = it.next();
            rankedDocIDs.add(pair.getKey());
            //System.out.println("docid: " + pair.getKey() + ", BM25 score: " + pair.getValue());
            i++;
        }
        return rankedDocIDs;
    }

    private static Map<Integer, Double> sortByComparator(Map<Integer, Double> unsortMap, final boolean order)
    {

        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
        {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static List<String> condenseArray(int same, List<String> longArray){
        int newSize = longArray.size()-1;
        List<String> shortArray = new ArrayList<>();

        for(int i = 0; i<same; i++){
            shortArray.add(longArray.get(i));
        }

        for(int i = same; i<newSize; i++){
            shortArray.add(longArray.get(i+1));
        }

        return shortArray;
    }

    public static int getSentenceScore(ArrayList<String> tokenizedSentence, List<Integer> tokenizedQueryID, lexicon lexi){
        //scoring will be based off how many of the query terms show up in a sentence that is less than 350 chars
        int score = 0;
        List<Integer> tokenizedSentenceID = transformtoTokenID(tokenizedSentence, lexi);
        for (int i = 0; i<tokenizedSentence.size(); i++){
            List<Integer> common = new ArrayList<>(tokenizedQueryID);
            common.retainAll(tokenizedSentenceID);
            score = common.size();
        }
        return score;
    }

    public static String getHeadlineSnippet(String docno, String date) throws IOException {
        //get raw file
        String raw;
        String folderName = "C:\\Users\\melwa\\Documents\\Melly Items\\University Items\\3A\\MSCI 541\\Extracted3\\" + date;
        String fileName = docno;

        FileReader rawReader = new FileReader(folderName + "\\"+ fileName + ".txt");
        BufferedReader rawBuffer = new BufferedReader(rawReader);

        String snippet = "";
        while((raw = rawBuffer.readLine())!= null){
            if(raw.contains("<TEXT>")){
                raw = rawBuffer.readLine();
                raw = rawBuffer.readLine();
                if(raw.length() >50){
                    snippet = raw.substring(0,50) + " ...";
                    break;
                }
                else{
                    snippet = raw.substring(0, raw.length()) + " ...";
                    break;
                }
            }
            else if(raw.contains("<GRAPHIC>")){
                raw = rawBuffer.readLine();
                raw = rawBuffer.readLine();
                if(raw.length() >50){
                    snippet = raw.substring(0,50) + " ...";
                    break;
                }
                else{
                    snippet = raw.substring(0, raw.length()) + " ...";
                    break;
                }
            }
        }

        return snippet;
    }
}
