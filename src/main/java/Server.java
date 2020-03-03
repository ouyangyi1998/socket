import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import pojo.Request;
import pojo.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends Thread{
    public Scanner in = new Scanner(System.in);

    public InputStreamReader isr;
    public BufferedReader br;

    public OutputStreamWriter osw;
    public  BufferedWriter rw;

    public Socket socket;
    public ServerSocket server;

    public Response responseReadCard = new Response();
    public Response responseFinalMsg = new Response();

    //配置端口与ip地址
    public Server(int port)
    {
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(10000);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(1234);
        server.start();//开启读取平台信息线程
    }


    //读取平台发送到终端的信息
    @Override
    public void run() {
        super.run();
        try {
            System.out.println("等待终端连接");
            socket = server.accept();
            System.out.println("终端连接成功");
            //接收平台传递数据
            isr = new InputStreamReader(socket.getInputStream());
            br = new BufferedReader(isr);
            //平台数据返还
            String str = br.readLine();
            JSONObject object = JSONObject.parseObject(str);
            //获取到传递来的数据
            String sn = object.getString("sn");
            String timestamp = object.getString("timestamp");
            String trans_code = object.getString("trans_code");
            Integer seq = object.getInteger("seq");
            responseReadCard.setSn(sn);
            responseReadCard.setTimestamp(timestamp);
            responseReadCard.setSeq(seq);
            responseReadCard.setRsp_code("1");
            responseReadCard.setTrans_code("01");
            //输出获取数据
            System.out.println("平台接收到的解码请求");
            System.out.println("sn:"+sn);
            System.out.println("timestamp:"+timestamp);
            System.out.println("trans_code:"+trans_code);
            System.out.println("seq:"+seq);
            //数据返回
           /* isr.close();
            br.close();*/
            new sendSecondMsg().start();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class  sendSecondMsg extends Thread{
        @Override
        public void run() {
            try {

                osw = new OutputStreamWriter(socket.getOutputStream());
                rw = new BufferedWriter(osw);

                String data = JSON.toJSONString(responseReadCard);//类型转换
                System.out.println("平台发送读卡命令");
                rw.write(data+"\n");//数据写入流
                rw.flush();
                //获取到平台01读卡指令
               /* osw.close();
                rw.close();*/
               new getCardMsg().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class getCardMsg extends Thread
    {
        @Override
        public void run() {
            try{
                isr = new InputStreamReader(socket.getInputStream());
                br = new BufferedReader(isr);
                //平台数据返还
                String str = br.readLine();
                JSONObject object = JSONObject.parseObject(str);
                //获取到传递来的数据
                String sn = object.getString("sn");
                String timestamp = object.getString("timestamp");
                String trans_code = object.getString("trans_code");
                Integer seq = object.getInteger("seq");
                String req_data = object.getString("req_data");

                System.out.println("终端返回的读卡数据");
                System.out.println("sn:"+sn);
                System.out.println("timestamp:"+timestamp);
                System.out.println("trans_code:"+trans_code);
                System.out.println("seq:"+seq);
                System.out.println("req_data:"+req_data);
                //这里回调函数返回数据，与云解码进行交互
               /* isr.close();
                br.close();*/
                new sendFinalMsg().start();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    class sendFinalMsg extends Thread{
        @Override
        public void run() {
            try {
                osw = new OutputStreamWriter(socket.getOutputStream());
                rw = new BufferedWriter(osw);
                System.out.println("返回给终端最终数据");
                responseFinalMsg.setTrans_code("03");
                responseFinalMsg.setRsp_code("1");
                responseFinalMsg.setSeq(2);
                responseFinalMsg.setSn(responseReadCard.getSn());
                responseFinalMsg.setTimestamp(responseReadCard.getTimestamp());
                responseFinalMsg.setRsp_data("读卡数据获取成功");

                String data = JSON.toJSONString(responseFinalMsg);//类型转换
                rw.write(data+"\n");//数据写入流
                rw.flush();
                osw.close();
                rw.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    /*public static void main(String[] args) {


        InputStreamReader isr;
        BufferedReader br;

        OutputStreamWriter osw;
        BufferedWriter rw;

        try {
            ServerSocket serverSocket=new ServerSocket( 1234);
            System.out.println("等待通讯连接。。。。");
            Socket socket=serverSocket.accept();
            //获取数据流
            isr=new InputStreamReader(socket.getInputStream());
            br=new BufferedReader(isr);
            String str=br.readLine();
            JSONObject object=JSONObject.parseObject(str);
            //获取到传递来的数据
            String sn=object.getString("sn");
            String timestamp=object.getString("timestamp");
            String trans_code=object.getString("trans_code");
            Integer seq=object.getInteger("seq");
            //输出获取数据
            System.out.println("sn:"+sn);
            System.out.println("timestamp:"+timestamp);
            System.out.println("trans_code:"+trans_code);
            System.out.println("seq:"+seq);
            //如果为解码出生请求平台
            if (trans_code.equals("00"))
            {
                //这里调用so库，注册回调函数
                //然后发送给终端读卡命令
                osw=new OutputStreamWriter(socket.getOutputStream());
                rw=new BufferedWriter(osw);

                Response response=new Response();
                response.setSn(sn);
                response.setRsp_code("1");
                response.setSeq(seq);
                response.setTimestamp(timestamp);
                response.setTrans_code("01");

                String data= JSON.toJSONString(response);//类型转换
                System.out.println(data);
                rw.write(data+"\n");//数据写入流
            }
            //关闭流
            br.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
}
