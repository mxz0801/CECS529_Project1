package cecs429.query;

import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;

import java.util.*;

public class WildcardLiteral implements Query {

    private String mTerm;

    public WildcardLiteral(String term) {
        mTerm = term;
    }
    public WildcardLiteral() {
    }
    public void setWildcardLiteral(String term) {
        mTerm =term;
    }


    @Override
    public List<Posting> getPostings(Index index) {
        return null;
    }

    @Override
    public List<Posting> getPostings(Index index, KgramIndex index2) {
        String dollar = Character.toString('$');
        String newTerm = dollar + mTerm + dollar;
        List<String> kGrams = new ArrayList<String>();
        String[] subString = newTerm.split("\\*");
        for (String sub : subString) {
            if(sub.length()==1 && sub.contains("$"))
                continue;
            if (sub.length() <= 3)
                kGrams.add(sub);
            else {
                for (int i = 0; i < sub.length() - 2; i++)
                    kGrams.add(sub.substring(i, i + 3));
            }
        }
        List<String> intersection = new ArrayList<>();
        List<String> bufferList = new ArrayList<>();
        for(String s : kGrams){
            if(s == kGrams.get(0)) {
                bufferList = index2.getGrams(s);
                if(kGrams.size() == 1)
                    intersection = bufferList;
                continue;
            }
            int i = 0;
            int j = 0;
            while(i < bufferList.size() && j < index2.getGrams(s).size()){
                if(bufferList.get(i).compareTo(index2.getGrams(s).get(j)) == 0) {
                    if(intersection.contains(bufferList.get(i))==false)
                        intersection.add(bufferList.get(i));
                    i++;
                    j++;
                }
                else if(bufferList.get(i).compareTo(index2.getGrams(s).get(j)) < 0 )
                    i++;
                else  if(bufferList.get(i).compareTo(index2.getGrams(s).get(j)) > 0)
                    j++;
            }
            bufferList = intersection;
        }
        //post-filtering
        subString = mTerm.split("\\*");
        int subFlag = 1;
        String reg = subString[0] + ".*";
        while (subFlag < subString.length) {
            reg = reg + subString[subFlag] + ".*";
            subFlag++;
        }
        Iterator<String> iterator = intersection.iterator();
        while(iterator.hasNext()){
            String s = iterator.next();
            if(mTerm.matches(reg) == false){
                iterator.remove();
            }
        }
        List<Posting> result = new ArrayList<>();
        List<Posting> bufferList2 = new ArrayList<>();
        //OR the posting
        for(String s : intersection){
            if(intersection.size() == 1){
                result = index.getPostings(s);
            }
            else {
                if (s == intersection.get(0)) {
                    bufferList2 = index.getPostings(s);
                    continue;
                }
                int i = 0;
                int j = 0;
                while (i <= bufferList2.size() && j <= index.getPostings(s).size()) {
                    if (i == bufferList2.size()) {
                        for (; j < index.getPostings(s).size(); j++)
                            result.add(index.getPostings(s).get(j));
                        break;
                    } else if (j == index.getPostings(s).size()) {
                        for (; i < bufferList2.size(); i++)
                            result.add(bufferList2.get(i));
                        break;
                    }

                    if (bufferList2.get(i).getDocumentId() == index.getPostings(s).get(j).getDocumentId()) {
                        result.add(bufferList2.get(i));
                        i++;
                        j++;
                    } else if (bufferList2.get(i).getDocumentId() < index.getPostings(s).get(j).getDocumentId()) {
                        result.add(bufferList2.get(i));
                        i++;
                    } else if (bufferList2.get(i).getDocumentId() > index.getPostings(s).get(j).getDocumentId()) {
                        result.add(index.getPostings(s).get(j));
                        j++;
                    }
                }
                bufferList2 = result;
            }
        }


        return result;
    }


    @Override
    public String toString() {
        return mTerm;
    }
}
