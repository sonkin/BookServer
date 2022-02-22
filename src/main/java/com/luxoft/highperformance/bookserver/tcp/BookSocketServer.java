package com.luxoft.highperformance.bookserver.tcp;

import com.luxoft.highperformance.bookserver.BookController;
import com.luxoft.highperformance.bookserver.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;

@Service
public class BookSocketServer extends Thread {

    @Autowired
    BookController bookController;

    @PostConstruct
    public void runServer() {
        start();
    }

    ExecutorService executorService =
            //Executors.newFixedThreadPool(4);
            new ThreadPoolExecutor(4, 4,
                    0,TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(100));

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9100);) {
            while (true) {
                Socket accept = serverSocket.accept();
                executorService.execute(() -> {
                    try (
                        DataInputStream in = new DataInputStream(
                                new BufferedInputStream(
                                        accept.getInputStream()));
                        DataOutputStream out = new DataOutputStream(
                                new BufferedOutputStream(
                                        accept.getOutputStream()))) {

                        int amount = in.readInt();

                        //System.out.println("Request "+amount+" books");
                        List<Book> books = bookController.getBooks(amount);

                        // write amount of data to be sent
                        ByteBuffer buffer = ByteBuffer.allocate(400*amount);
                        int size = 0;
                        for (Book b : books) {
                            size += b.toBinary(buffer);
                        }
                        out.writeInt(size);
                        out.flush();
                        byte[] data = buffer.array();
                        out.write(data, 0, size);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            accept.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } // while
        } catch (Exception exception) { // auto-close server socket
            exception.printStackTrace();
        }
    } // method


}
