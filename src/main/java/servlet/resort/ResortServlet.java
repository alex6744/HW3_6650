package servlet.resort;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import servlet.channelPool.ChannelPool;
import servlet.skier.MyLiftRider;
import servlet.skier.PostResponse;

@WebServlet(name = "ResortServlet", value = "/resorts/*")
public class ResortServlet extends HttpServlet {
  public ChannelPool channelPool;

  @Override
  public void init(ServletConfig config){
    channelPool=new ChannelPool();
    channelPool.init();

  }
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

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



      Channel channel=channelPool.take();



      // System.out.println("bbbbbbbb");

      ResortLiftRider myLiftRider =(ResortLiftRider) gson.fromJson(bodyContentToString(request),
          ResortLiftRider.class);
      System.out.println(myLiftRider.getYear());

      ResortPostResponse postRespone=new ResortPostResponse(resortID,myLiftRider.getYear());
      channel.queueDeclare("resort_post",false,false,false,null);
      channel.basicPublish("","resort_post",null,postRespone.toString().getBytes(StandardCharsets.UTF_8));
      channelPool.add(channel);
      //response.getWriter().write(myLiftRider.getYear());
      //response.setContentType("application/json");
      // response.getWriter().write(new Gson().toJson(myLiftRider));
      response.setStatus(HttpServletResponse.SC_CREATED);

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
    if(urlPath.length!=3){
      return false;
    }
    if(!urlPath[2].equals("seasons")){
      return false;
    }

    return true;
  }
}
