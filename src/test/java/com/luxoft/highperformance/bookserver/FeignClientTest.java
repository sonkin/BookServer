package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.model.Book;
import one.nio.serial.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FeignClientTest {

    @Autowired
    BookClient bookClient;

    @Autowired
    BookController bookController;

    @Test
    void testGetPerson() {
        System.out.println(bookClient.baseline("hello"));
    }


    @Test
    public void onenio() throws IOException, ClassNotFoundException {
        List<Book> books = bookController.getBooks(10);
        CalcSizeStream css = new CalcSizeStream();
        css.writeObject(books);
        int length = css.count();

        byte[] buf = new byte[length];
        SerializeStream out = new SerializeStream(buf);
        out.writeObject(books);
        assertEquals(out.count(), length);

        DeserializeStream in = new DeserializeStream(buf);
        Object objCopy = in.readObject();
        System.out.println(objCopy.toString());
    }


}