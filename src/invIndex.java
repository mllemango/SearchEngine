//

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class invIndex {
    public Map<Integer, ArrayList<posting>> invIndex = new HashMap<Integer, ArrayList<posting>>();


    public void addPosting(Integer termID, Integer count, Integer docID){
        ArrayList<posting> postingList = new ArrayList<>();
        posting temp = new posting(docID, count);
        if (!invIndex.containsKey(termID)){
            postingList.add(temp);
            invIndex.put(termID, postingList);
        }
        else {
            postingList = invIndex.get(termID);
            postingList.add(temp);
            invIndex.put(termID, postingList);
        }

    }

    public ArrayList<posting> getPostings(Integer termID){
        ArrayList<posting> postings = invIndex.get(termID);
        return postings;
    }

    public void print(PrintWriter out){
        String output = "Inverted Index";
        out.println(output);
        for(Map.Entry<Integer, ArrayList<posting>> entry1 : invIndex.entrySet()){
            Integer termid = entry1.getKey();
            ArrayList<posting> postings = entry1.getValue();

            String printedPosting=postings.get(0).print();
            for(int i = 1; i<postings.size(); i++){
                printedPosting = printedPosting + ";" + postings.get(i).print();
            }

            //System.out.println(outputKey1 + ";" + outputValue1);
            output = termid + ":" + printedPosting + ";";
            out.println(output);
            out.flush();

        }
    }

}
