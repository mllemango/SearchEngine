import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class lexicon {

    public Map<String, Integer> term_id = new HashMap<String, Integer>();
    public Map<Integer, String> id_term = new HashMap<Integer, String>();
    public Integer idCount = 0;

    public void addTerm(String word){
        id_term.put(idCount, word);
        term_id.put(word, idCount);
        idCount++;
    }

    public boolean contains(String word){
        boolean contains = term_id.containsKey(word);
        return contains;
    }

    public Map<String, Integer> getTerm_id(){
        return term_id;
    }

    public Map<Integer, String> getId_term(){
        return id_term;
    }

    public Integer getID(String term){
        Integer id = 0;
        id= term_id.get(term);
        return id;
    }

    public String getString (Integer id){
        String word;
        word = id_term.get(id);
        return word;
    }

    public void print(PrintWriter out){
        String output="Lexicon";
        out.println(output);
        //out.println("Lexicon size: " + id_term.size());
        for(Map.Entry<Integer, String> entry1 : id_term.entrySet()){
            Integer id = entry1.getKey();
            String term = entry1.getValue();

            //System.out.println(outputKey1 + ";" + outputValue1);
            output = id + ";" + term;
            out.println(output);
            out.flush();

        }
    }
}

