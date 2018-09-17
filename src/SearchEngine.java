import java.io.*;
import java.util.*;

public class SearchEngine {

    public static void main(String[] args) throws IOException {

        rankingObjects objCollection = readingTxtFiles();
        lexicon lexi = objCollection.getLexi();
        Map<Integer, metadata> id_data = objCollection.getId_data();

        //get user's query
        //String input = getInput();
        //System.out.println("user entered: " + input);
        String input = "";
        Scanner reader = new Scanner(System.in);

        while(!input.contains("q")) {
            System.out.println("Please enter search term(s)");
            input = reader.nextLine();

            //---------starting to calculate runtime of program-------------//
            long starttime = System.nanoTime();

            //tokenize query
            ArrayList<Integer> tokenIDs = new ArrayList<>();
            tokenIDs = transformtoTokenID(tokenize(input), lexi);

            //BM25 gives us a queue of doc results
            //PriorityQueue<Integer> searchResultDocNos = BM25(objCollection, tokenIDs);
            ArrayList<Integer> searchResultDocNos = BM25(objCollection, tokenIDs);

            //get QB summaries from raw file
            HashMap<Integer, String> resultDoc_Summary = new HashMap<>();
            for (int i = 0; i < searchResultDocNos.size(); i++) {
                //Integer docid = searchResultDocNos.remove();
                Integer docid = searchResultDocNos.get(i);
                String date = id_data.get(docid).getDate();
                String docno = id_data.get(docid).getDocno();

                resultDoc_Summary.put(docid, getQBSummary(docno, date, tokenIDs, lexi));

            }

            //logic for printing out the results
            int i = 1;
            for (Map.Entry<Integer, String> entry : resultDoc_Summary.entrySet()) {
                Integer docid = entry.getKey();
                String QBSummary = entry.getValue();
                String headline = id_data.get(docid).getHeadline();
                String date = id_data.get(docid).getDate();
                String docno = id_data.get(docid).getDocno();

                if (headline.isEmpty()) {
                    headline = getHeadlineSnippet(docno, date);
                }
                //parsing date
                date = date.substring(0, 2) + "/" + date.substring(2, 4) + "/19" + date.substring(4, 6);

                System.out.println(i + ". " + headline + " (" + date + ") ");
                System.out.println(QBSummary + " (" + docno + ")");
                System.out.println();
                i++;
            }

            //--------end time of program---------//
            long endtime = System.nanoTime();
            double runtime = ((double) endtime - starttime) / 1000000000.0;

            System.out.println("Retrieval took " + runtime + " seconds");
            System.out.println();


            System.out.println("Enter N for new query, Q to quit program");
            input = reader.nextLine();
            input = input.toLowerCase();
            while((!input.contains("q")) && (!input.contains("n"))){
                System.out.println("invalid entry, please try again. \nEnter N for new query, Q to quit program");
                input = reader.nextLine();
                input = input.toLowerCase();
            }
        }

        reader.close();
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

    public static String getQBSummary(String docno, String date, ArrayList<Integer> queryTokens, lexicon lexi) throws IOException {
        //get raw file
        String raw;
        String folderName = "C:\\Users\\melwa\\Documents\\Melly Items\\University Items\\3A\\MSCI 541\\Extracted3\\" + date;
        String fileName = docno;

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
                    //System.out.println("sentence 1: " + sentences.get(i));
                    //System.out.println("sentence 2: " + sentences.get(i+1));
                    sentences.set(i, sentences.get(i) + sentences.get(i+1));
                    //System.out.println("new merge: " + sentences.get(i));
                    //System.out.println();
                    sentences = condenseArray(i+1, sentences);
                    i--;
                }
            }
            i++;
        }

        int sentenceScore = 0;
        for(int j = 0; j<sentences.size(); j++){
            int tempScore = getSentenceScore(tokenize(sentences.get(j)), queryTokens, lexi);
            if(tempScore > sentenceScore){
                QBSummary = sentences.get(j);
                sentenceScore = tempScore;
            }
        }

        return QBSummary;

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
