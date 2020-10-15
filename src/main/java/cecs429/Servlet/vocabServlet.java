package cecs429.Servlet;

import cecs429.csulb.SearchEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        SearchEngine searchEngine = new SearchEngine();

        List<String> vocabulary = searchEngine.getVocab();
        List<String> temp = new ArrayList<>();
        for(String vocab : vocabulary){
            temp.add(vocab+"<br />");
        }
        mapper.writeValue(response.getWriter(),temp);

    }
}
