package cecs429.query;

import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;
import cecs429.text.ImprovedTokenProcessor;

import java.util.ArrayList;
import java.util.List;

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
        List<String> kGrams = new ArrayList<>();
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
        for(String s: kGrams){
            if(s.equals(kGrams.get(0))) {
                intersection.addAll(index2.getGrams(s));
                continue;
            }
            List<String> bufferList = new ArrayList<>(index2.getGrams(s));
            intersection.retainAll(bufferList);
        }
        /*
        for(String s : kGrams){
            if(s == kGrams.get(0)) {
                intersection = index2.getGrams(s);
                continue;
            }
            int i = 0;
            int j = 0;
            List<String> bufferList = new ArrayList<>();
            while(i < intersection.size() && j < index2.getGrams(s).size()){
                if(intersection.get(i).compareTo(index2.getGrams(s).get(j)) == 0) {
                        bufferList.add(intersection.get(i));
                    i++;
                    j++;
                }
                else if(intersection.get(i).compareTo(index2.getGrams(s).get(j)) < 0 )
                    i++;
                else  if(intersection.get(i).compareTo(index2.getGrams(s).get(j)) > 0)
                    j++;
            }
            intersection = bufferList;
        }

         */

        //post-filtering
        subString = mTerm.split("\\*");
        int subFlag = 1;
        StringBuilder reg = new StringBuilder(subString[0] + ".*");
        while (subFlag < subString.length) {
            reg.append(subString[subFlag]).append(".*");
            subFlag++;
        }
        intersection.removeIf(s -> !mTerm.matches(reg.toString()));
        /*
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
            if(!mTerm.matches(reg)){
                iterator.remove();
            }
        }
         */
        List<Posting> result = new ArrayList<>();

        //OR the posting
        for(String str : intersection) {

            String s = "";
            try {
                s = getStem(str);
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            if (s.equals(intersection.get(0))) {
                result = index.getPostings(s);
                continue;
            }
            List<Posting> bufferList = new ArrayList<>();
            int i = 0;
            int j = 0;
            while (i <= result.size() && j <= index.getPostings(s).size()) {
                if (i == result.size()) {
                    for (; j < index.getPostings(s).size(); j++)
                        bufferList.add(index.getPostings(s).get(j));
                    break;
                } else if (j == index.getPostings(s).size()) {
                    for (; i < result.size(); i++)
                        bufferList.add(result.get(i));
                    break;
                }
                if (result.get(i).getDocumentId() == index.getPostings(s).get(j).getDocumentId()) {
/*
                    ArrayList<Integer> position = new ArrayList<>();
                    //combine the position
                    int h = 0;
                    int k = 0;
                    ArrayList<Integer> a = new ArrayList<>(result.get(i).getPosition());
                    ArrayList<Integer> b = new ArrayList<>(index.getPostings(s).get(j).getPosition());
                    while(h <= result.get(i).getPosition().size() && k <= index.getPostings(s).get(j).getPosition().size()){
                        if(h == result.get(i).getPosition().size()){
                            for(; k < index.getPostings(s).get(j).getPosition().size(); k++)
                                position.add(index.getPostings(s).get(j).getPosition().get(k));
                            break;
                        }
                        if(k == index.getPostings(s).get(j).getPosition().size()){
                            for(; h < result.get(i).getPosition().size(); h++)
                                position.add(result.get(i).getPosition().get(h));
                            break;
                        }
                        if(result.get(i).getPosition().get(h) == index.getPostings(s).get(j).getPosition().get(k)) {
                            position.add(result.get(i).getPosition().get(h));
                            h++;
                            k++;
                        }
                        else if(result.get(i).getPosition().get(h) < index.getPostings(s).get(j).getPosition().get(k)){
                            position.add(result.get(i).getPosition().get(h));
                            h++;
                        }
                        else  if(result.get(i).getPosition().get(h) > index.getPostings(s).get(j).getPosition().get(k)){
                            position.add(index.getPostings(s).get(j).getPosition().get(k));
                            k++;
                        }
                    }
                    Posting p = new Posting(result.get(i).getDocumentId(), position);
                    bufferList.add(p);

 */


                  bufferList.add(result.get(i));
                    i++;
                    j++;
                } else if (result.get(i).getDocumentId() < index.getPostings(s).get(j).getDocumentId()) {
                    bufferList.add(result.get(i));
                    i++;
                } else if (result.get(i).getDocumentId() > index.getPostings(s).get(j).getDocumentId()) {
                    bufferList.add(index.getPostings(s).get(j));
                    j++;
                }
            }
            result = new ArrayList<>(bufferList);
        }
        return result;
        }

    public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
        return processor2.stem(input);
    }
    @Override
    public String toString() {
        return mTerm;
    }
}


