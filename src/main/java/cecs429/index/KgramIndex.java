package cecs429.index;

import java.util.*;

public class KgramIndex implements Index{
    private Map<String, ArrayList<String>> kIndex= new HashMap<>();
    private List<String> mVocabulary = new ArrayList<>();

    /**
     * Build 3-gram for the given term
     */
    public void addTerm(String term) {
        ArrayList<String> kGram = new ArrayList<>();

        if (term.length() < 3) {
            if (kIndex.get(term) == null) {
                ArrayList<String> TermList = new ArrayList<>();
                TermList.add(term);
                kIndex.put(term, TermList);
            }
        }
        else {
            String dollar = Character.toString('$');
            String newTerm = dollar + term + dollar;
            for (int i = 0; i < term.length(); i++) { //1-gram
                kGram.add(Character.toString(term.charAt(i)));
            }
            for (int i = 0; i < newTerm.length() - 1; i++) { //2-grams
                kGram.add(newTerm.substring(i, i + 2));
            }
            for (int i = 0; i < newTerm.length() - 2; i++) { //3-grams
                kGram.add(newTerm.substring(i, i + 3));
            }
            for (String s : kGram) {
                if (kIndex.get(s) == null) {
                    ArrayList<String> TermList = new ArrayList<>();
                    TermList.add(term);
                    kIndex.put(s, TermList);
                } else {
                    kIndex.get(s).add(term);
                }
            }
        }
        mVocabulary.add(term);
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
