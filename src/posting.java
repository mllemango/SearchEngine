public class posting {
    public int docID;
    public int count;

    public posting(int docID, int count){
        this.docID = docID;
        this.count = count;
    }
    public int getDocID(){
        return docID;
    }

    public int getCount(){
        return count;
    }

    public String print(){
        String output = docID+";"+count;
        return output;
    }

}
