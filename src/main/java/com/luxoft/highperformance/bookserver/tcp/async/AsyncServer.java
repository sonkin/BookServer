package com.luxoft.highperformance.bookserver.tcp.async;

import com.luxoft.highperformance.bookserver.BookController;
import com.luxoft.highperformance.bookserver.model.Book;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class AsyncServer extends Thread {

    @Autowired
    BookController bookController;

    @PostConstruct
    public void init() {
        start();
    }

    @Override
    public void run() {
        try(AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()) {
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 3883);
            serverChannel.bind(hostAddress);
            log.log(Level.INFO,"Server channel bound to port: " + hostAddress.getPort());

            nextClient(serverChannel).get();
        } catch (Exception e) {
            log.log(Level.ERROR, "Wasn't able to run AsyncServer");
        }
    }

    private CompletableFuture<Void> nextClient(AsynchronousServerSocketChannel serverChannel) {
        return NioCF.acceptClient(serverChannel)
            .thenCompose(clientChannel -> NioCF.readInt(clientChannel)
                .thenCompose(amount -> bookController.getBooksCF(amount)
                .thenCompose(books -> {
                    ByteBuffer booksBuffer = ByteBuffer.allocate(400*amount);
                    int size = 4;
                    // initial size - leave space to put the correct size later
                    booksBuffer.putInt(size);
                    for (Book b : books) {
                        size += b.toBinary(booksBuffer);
                    }
                    booksBuffer.position(0); // jump to start and put a real size
                    booksBuffer.putInt(size);
                    booksBuffer.position(0); // prepare to slice
                    booksBuffer.limit(size);
                    return NioCF.writeBuffer(booksBuffer.slice(), clientChannel);
                })
                .thenRun(()-> NioCF.close(clientChannel))

            ))

            // also we are able to use recursion instead of while(true)
            .thenCompose((Void v)->nextClient(serverChannel));
    }

}