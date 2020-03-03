import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import pojo.Request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
//模拟终端发送请求
public class Client extends Thread{
    public Scanner in = new Scanner(System.in);

    public InputStreamReader isr;
    public BufferedReader br;

    public OutputStreamWriter osw;
    public  BufferedWriter rw;

    public Socket socket;

    public Request requestFirst = new Request();

    //配置端口与ip地址
    public Client(String host,int port)
    {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1",1234);
        client.start();//开启读取平台信息线程
    }


    //读取平台发送到终端的信息
    @Override
    public void run() {
        try {
            //开启初始请求线程
            new sendFirstMsg().start();
            //接收平台传递数据
            isr = new InputStreamReader(socket.getInputStream());
            br = new BufferedReader(isr);
            //平台数据返还
            String str = br.readLine();
            JSONObject object = JSONObject.parseObject(str);

            //获取到传递来的数据、
            System.out.println("终端接收到读卡命令");
            String sn = object.getString("sn");
            String timestamp = object.getString("timestamp");
            String trans_code = object.getString("trans_code");
            Integer seq = object.getInteger("seq");
            //输出获取数据
            System.out.println("sn:"+sn);
            System.out.println("timestamp:"+timestamp);
            System.out.println("trans_code:"+trans_code);
            System.out.println("seq:"+seq);
            //输出给平台读卡数据
          /*  isr.close();
            br.close();*/
            new sendReadCardMsg().start();

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

class  sendFirstMsg extends Thread{
    @Override
    public void run() {
        try {

            osw = new OutputStreamWriter(socket.getOutputStream());
            rw = new BufferedWriter(osw);
            System.out.println("终端发送解码请求");
            //注入sn码
            requestFirst.setSn("123456789");
            //注入时间戳
            requestFirst.setTimestamp("20200203");
            //注入业务类型
            requestFirst.setTrans_code("00");
            //注入指令序列
            requestFirst.setSeq(1);
            requestFirst.setReq_data("解码请求");
            String data = JSON.toJSONString(requestFirst);//类型转换
            rw.write(data+"\n");//数据写入流
            rw.flush();
            //获取到平台01读卡指令
               /* rw.close();
                socket.close();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class sendReadCardMsg extends Thread{
    @Override
    public void run() {
        try {
            Request requestSecond = new Request();
            requestSecond.setSeq(2);
            System.out.println("终端返回读卡数据");
            requestSecond.setTrans_code("02");
            requestSecond.setTimestamp(requestFirst.getTimestamp());
            requestSecond.setSn(requestFirst.getSn());
            requestSecond.setReq_data("读卡数据");

            String data = JSON.toJSONString(requestSecond);
            rw.write(data + "\n");
            rw.flush();

            new getFinalMsg().start();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
class getFinalMsg extends Thread{
    @Override
    public void run() {
        try {
            isr = new InputStreamReader(socket.getInputStream());
            br = new BufferedReader(isr);
            //平台数据返还
            Thread.sleep(5000);
            String str = br.readLine();
            JSONObject object = JSONObject.parseObject(str);

            //获取到传递来的数据
            String sn = object.getString("sn");
            String timestamp = object.getString("timestamp");
            String trans_code = object.getString("trans_code");
            String rsp_data = object.getString("rsp_data");
            Integer seq = object.getInteger("seq");
            //输出获取数据
            System.out.println("终端获取到最终返回结果");
            System.out.println("sn:" + sn);
            System.out.println("timestamp:" + timestamp);
            System.out.println("trans_code:" + trans_code);
            System.out.println("rsp_data:"+rsp_data);
            System.out.println("seq:" + seq);
            System.out.println("socket过程结束");
            //输出给平台读卡数据
            isr.close();
            br.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
}
