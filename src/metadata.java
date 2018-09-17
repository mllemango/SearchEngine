public class metadata {
    public String docno;
    public Integer in_ID = new Integer(0);
    public String date;
    public String headline;
    public int wordCount;

    public metadata(String docno, String date, String headline){
        this.docno = docno;
        this.date = date;
        this.headline = headline;
    }

    public void setID(int new_ID){
        in_ID = new_ID;
    }

    public void setWordCount(int count){
        wordCount = count;
    }

    public void addHeadline(String newHeadline){
        headline = newHeadline;
    }

    public String print(){
        String output = docno + ";" + date + ";" + headline + "~" + wordCount;
        return output;
    }

    public String getDocno(){
        return docno;
    }

    public String getDate(){
        return date;
    }

    public String getHeadline(){
        return headline;
    }

    public int getWordCount (){
        return  wordCount;
    }
}
