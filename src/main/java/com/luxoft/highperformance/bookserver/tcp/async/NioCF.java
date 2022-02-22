package com.luxoft.highperformance.bookserver.tcp.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NioCF {

    public static CompletableFuture<Void> connect(
            AsynchronousSocketChannel channel,
            InetSocketAddress hostAddress)
            throws IOException {
        CompletableFuture<Void> futureSocket = new CompletableFuture<>();
        channel.connect(hostAddress, null,
                new CompletionHandler<Void, Void>() {
                    @Override
                    public void completed(Void result, Void serverChannel) {
                        futureSocket.complete(null);
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        futureSocket.completeExceptionally(exc);
                    }
                });

        return futureSocket;
    }

    public static CompletableFuture<Void> writeBuffer(ByteBuffer buffer, AsynchronousSocketChannel clientChannel){
        CompletableFuture<Void> futureWrite = new CompletableFuture<>();

        clientChannel.write(buffer,
                1, TimeUnit.SECONDS,
                null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                futureWrite.complete(null);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
                futureWrite.completeExceptionally(exc);
            }
        });

        return futureWrite;
    }

    public static CompletableFuture<String> read(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(64);
        CompletableFuture<String> futureMsg = new CompletableFuture<>();

        clientChannel.read(buffer, buffer,
                new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer buf) {
                        buf.flip();
                        byte[] bytes = new byte[buf.remaining()];
                        buf.get(bytes);
                        String message = new String(bytes).trim();
                        futureMsg.complete(message);
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        exc.printStackTrace();
                        futureMsg.completeExceptionally(exc);
                    }
                });
        return futureMsg;
    }

    public static CompletableFuture<Integer> readInt(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        CompletableFuture<Integer> futureMsg = new CompletableFuture<>();

        clientChannel.read(buffer, buffer,
                new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer buf) {
                        buf.flip();
                        int bufInt = buf.getInt();
                        futureMsg.complete(bufInt);
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        exc.printStackTrace();
                        futureMsg.completeExceptionally(exc);
                    }
                });
        return futureMsg;
    }

    public static CompletableFuture<AsynchronousSocketChannel> acceptClient(
            AsynchronousServerSocketChannel serverChannel) {
        CompletableFuture<AsynchronousSocketChannel> futureSocket = new CompletableFuture<>();
        serverChannel.accept(null,
                new CompletionHandler<AsynchronousSocketChannel, Void>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Void attachment) {
                        futureSocket.complete(result);
                    }
                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        exc.printStackTrace();
                        futureSocket.completeExceptionally(exc);
                    }
                });
        return futureSocket;
    }

    public static void close(AsynchronousChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    public static String readConsole() {
        try {
            return console.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
