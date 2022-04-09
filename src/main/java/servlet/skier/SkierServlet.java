package servlet.skier;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import servlet.channelPool.ChannelPool;

@WebServlet(name = "servlet.skier.SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
  public  ChannelPool channelPool;

  @Override
  public void init(ServletConfig config){
    channelPool=new ChannelPool();
    channelPool.init();

  }
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String urlPath = request.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }
    String[] urlParts = urlPath.split("/");
    if(!isUrlValid(urlParts)){
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("url is not valid");
      return;
    }
    response.setStatus(HttpServletResponse.SC_OK);


   PrintWriter out = response.getWriter();
   //out.println("<h1>" + "msg" + "</h1>");
    out.println("1");
   // response.getWriter().write(request.getPathInfo());

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      String urlPath = request.getPathInfo();
      Gson gson=new Gson();
      if (urlPath == null || urlPath.isEmpty()) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("missing parameters");
        return;
      }
      String[] urlParts = urlPath.split("/");
      if(!isUrlValid(urlParts)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("url is not valid");
        return;
      }
    System.out.println(urlPath);

      try {
        int resortID=Integer.parseInt(urlParts[1]);
        String seasonID=urlParts[3];
        String dayID=urlParts[5];
        int skierID=Integer.parseInt(urlParts[7]);


        Channel channel=channelPool.take();
        PostResponse postRespone=new PostResponse(resortID,seasonID,dayID,skierID);
        channel.queueDeclare("post",false,false,false,null);
        channel.basicPublish("","post",null,postRespone.toString().getBytes(StandardCharsets.UTF_8));
        channelPool.add(channel);


       // System.out.println("bbbbbbbb");
        response.setStatus(HttpServletResponse.SC_CREATED);
        MyLiftRider myLiftRider =(MyLiftRider) gson.fromJson(bodyContentToString(request),
            MyLiftRider.class);

        //response.setContentType("application/json");
       // response.getWriter().write(new Gson().toJson(myLiftRider));

      }catch (IllegalArgumentException | InterruptedException e){
        e.printStackTrace();
        response.getWriter().write("missing parameters");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

      }



  }
  public String bodyContentToString(HttpServletRequest request) throws IOException {
    BufferedReader bufferedReader=request.getReader();
    StringBuilder sb=new StringBuilder();
    String line;
    while((line=bufferedReader.readLine())!=null){
      sb.append(line);
    }
    return sb.toString();
  }

  private  boolean isUrlValid(String[] urlPath){
    if(urlPath.length<3){
      return false;
    }
    if(!urlPath[2].equals("seasons")){
      return false;
    }
    if(urlPath[2].equals("seasons")){
      if(urlPath[4].equals("days")){
        if(!urlPath[6].equals("skiers")){
          return false;
        }
      }else{
        return false;
      }
    }
    return true;
  }

}
