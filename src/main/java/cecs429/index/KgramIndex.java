package cecs429.index;

import java.util.*;

public class KgramIndex implements Index{
    private Map<String, ArrayList<String>> kIndex= new HashMap<String, ArrayList<String>>();
    private List<String> mVocabulary = new ArrayList<String>();

    /**
     * Build 3-gram for the given term
     */
    public void addTerm(String term) {
        ArrayList<String> oneGram = new ArrayList<String>();
        ArrayList<String> twoGram = new ArrayList<String>();
        ArrayList<String> threeGram = new ArrayList<String>();

        if(mVocabulary.contains(term) == false) {
            if (term.length() == 1) {
                if (kIndex.get(term) == null) {
                    ArrayList<String> TermList = new ArrayList<String>();
                    TermList.add(term);
                    kIndex.put(term, TermList);
                } else
                    kIndex.get(term).add(term);
            }
            else {
                String dollar = Character.toString('$');
                String newTerm = dollar + term + dollar;
                for (int i = 0; i < term.length(); i++) { //1-gram
                    oneGram.add(Character.toString(term.charAt(i)));
                }
                for(String s : oneGram) {
                    if (kIndex.get(s) == null) {
                        ArrayList<String> TermList = new ArrayList<String>();
                        TermList.add(term);
                        kIndex.put(s, TermList);
                    }
                    else if(kIndex.get(s).contains(term) == false) {
                        kIndex.get(s).add(term);
                        Collections.sort(kIndex.get(s));
                    }
                }
                for (int i = 0; i < newTerm.length() - 1; i++) { //2-grams
                    twoGram.add(newTerm.substring(i, i + 2));
                }
                for(String s: twoGram) {
                    if (kIndex.get(s) == null) {
                        ArrayList<String> TermList = new ArrayList<String>();
                        TermList.add(term);
                        kIndex.put(s, TermList);
                    }
                    else if (kIndex.get(s).contains(term) == false) {
                        kIndex.get(s).add(term);
                        Collections.sort(kIndex.get(s));
                    }
                }

                for (int i = 0; i < newTerm.length() - 2; i++) { //3-grams
                    threeGram.add(newTerm.substring(i, i + 3));
                }
                for(String s: threeGram) {
                    if (kIndex.get(s) == null) {
                        ArrayList<String> TermList = new ArrayList<String>();
                        TermList.add(term);
                        kIndex.put(s, TermList);
                    }
                    else if (kIndex.get(s).contains(term) == false) {
                        kIndex.get(s).add(term);
                        Collections.sort(kIndex.get(s));
                    }
                }
            }
            mVocabulary.add(term);
        }
    }


    public List<String> getGrams(String term) {
        return kIndex.get(term);
    }
    @Override
    public List<Posting> getPostings(String term){
        return  null;
    }
    @Override
    public List<String> getVocabulary() {
        Collections.sort(mVocabulary);
        return mVocabulary;
    }

}
