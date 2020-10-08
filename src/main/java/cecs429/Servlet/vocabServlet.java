package cecs429.Servlet;

import cecs429.csulb.SearchEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "vocabServlet",urlPatterns = {"/vocab"})
public class vocabServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson=new Gson();
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        String dir = request.getParameter("dir");
        String input = request.getParameter("input");
        SearchEngine searchEngine = new SearchEngine();

      /*  try {
            searchEngine.search(dir,input);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
       }
       */

        List<String> vocabulary = searchEngine.getVocab();
        List<String> temp = new ArrayList<>();
        for(String vocab : vocabulary){
            temp.add(vocab+"<br />");
        }
        mapper.writeValue(response.getWriter(),temp);

    }
}
