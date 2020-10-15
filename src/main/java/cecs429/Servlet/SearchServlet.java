package cecs429.Servlet;

import cecs429.csulb.SearchEngine;
import cecs429.documents.GsonDoc;
import cecs429.index.Index;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
    SearchEngine searchEngine = new SearchEngine();
    ObjectMapper mapper = new ObjectMapper();
    Index index = null;

    public SearchServlet() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        List<String> vocabulary = searchEngine.getVocab();
        List<String> temp = new ArrayList<>();
        for (String vocab : vocabulary) {
            temp.add(vocab + "<br />");
        }
        mapper.writeValue(response.getWriter(), temp);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String dir = request.getParameter("dir");
        String input = request.getParameter("input");
        if(index==null){
            try {
                index = searchEngine.indexing(dir);
                List<GsonDoc> result = searchEngine.search(index, input);
                mapper.writeValue(response.getWriter(), result);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                List<GsonDoc> result = searchEngine.search(index, input);
                mapper.writeValue(response.getWriter(), result);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        }



    }

