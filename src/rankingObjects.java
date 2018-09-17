import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class rankingObjects {

    private Map<Integer, metadata> id_data = new HashMap<Integer, metadata>();
    private lexicon lexi = new lexicon();
    private invIndex invIndex = new invIndex();

    public rankingObjects(Map<Integer, metadata> metadataObj, lexicon lexicon, invIndex invertedIndex){

        this.id_data = metadataObj;
        this.lexi = lexicon;
        this.invIndex = invertedIndex;

    }

    public Map<Integer, metadata> getId_data(){
        return this.id_data;
    }

    public lexicon getLexi(){
        return this.lexi;
    }

    public invIndex getInvIndex(){
        return this.invIndex;
    }
}
