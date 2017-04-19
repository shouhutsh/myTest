package com.glbpay.bgw.service.channel;

import com.glbpay.common.util.validator.StringUtils;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shouhutsh on 2017/4/19.
 */
public class SimpleSocketServer {

    private static final ExecutorService excutor = Executors.newFixedThreadPool(5);

    private static final String TEMPLATE = "HTTP/1.0 200 OK\r\n"+
        "Server: OneFile 1.0\r\n"+
        "Content-length: %d\r\n"+
        "Content-type: application/json\r\n" +
        "\r\n" +
        "%s";

    public static void main(String[] args) throws Exception{
        ServerSocket server = new ServerSocket(20000);
        System.out.println("Start success!");
        try {
            while (true) {
                excutor.execute(new ServerThread(server.accept()));
            }
        } finally {
            server.close();
        }
    }

    private static class ServerThread implements Runnable {
        private Socket client = null;

        public ServerThread(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try{
                System.out.println("Connect success!");

                byte[] request = getRequest();

                byte[] response = getResponse(request);

                send(response);

                client.close();
                System.out.println("Connect closed!");
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        private void send(byte[] response) throws IOException {
            OutputStream output = new BufferedOutputStream(client.getOutputStream());
            output.write(response);
            output.flush();
        }

        private byte[] getRequest() throws IOException {
            int size, default_buffer_size = 100;
            InputStream input = new BufferedInputStream(client.getInputStream());

            byte[] request = new byte[0], buffer = new byte[default_buffer_size];
            while (default_buffer_size == (size = input.read(buffer))) {
                request = ByteUtils.concatenate(request, buffer);
            }
            request = ByteUtils.concatenate(request, ByteUtils.subArray(buffer, 0, size));
            return request;
        }

        private byte[] getResponse(byte[] request) throws Exception {
            byte[] reqContent = RequestType.getType(request).getContent(request);
            System.out.println("Request: " + new String(reqContent));
            String resContent = "hello";
            byte[] response = String.format(TEMPLATE, resContent.getBytes().length, resContent).getBytes();
            System.out.println("Response: " + resContent);
            return response;
        }
    }

    private enum RequestType{
        GET {
            @Override
            public byte[] getContent(byte[] request) throws Exception{
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request)));
                try {
                    Pattern p = Pattern.compile("GET.*?\\?([^ ]+).+");
                    Matcher m = p.matcher(reader.readLine());
                    if (m.find()) {
                        return m.group(1).getBytes();
                    }
                    return new byte[0];
                } finally {
                    reader.close();
                }
            }
        },
        POST {
            @Override
            public byte[] getContent(byte[] request) throws Exception{
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request)));
                try {
                    while (StringUtils.isNotBlank(reader.readLine())) {
                    }
                    return reader.readLine().getBytes();
                } finally {
                    reader.close();
                }
            }
        },
        OTHER {
            @Override
            public byte[] getContent(byte[] request) throws Exception{
                return request;
            }
        };

        public abstract byte[] getContent(byte[] request) throws Exception;

        public static RequestType getType(byte[] request) {
            if (startWith(request, "GET")) {
                return GET;
            } else if (startWith(request, "POST")) {
                return POST;
            } else {
                return OTHER;
            }
        }
    }

    private static boolean startWith(byte[] bytes, String str) {
        return ByteUtils.equals(str.getBytes(), ByteUtils.subArray(bytes, 0, str.getBytes().length));
    }
}
