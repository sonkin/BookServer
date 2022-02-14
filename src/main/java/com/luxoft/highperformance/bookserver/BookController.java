package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.measure.Measure;
import com.luxoft.highperformance.bookserver.model.Book;
import com.luxoft.highperformance.bookserver.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("books")
public class BookController {

    public final int BOOKS_AMOUNT=10_000;

    @Autowired
    BookRepository bookRepository;

    @Measure(value = "baseline", warmup = 50)
    @GetMapping("keywords0/{keywordsString}")
    public List<Book> getBookByTitle(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");
        if (keywords.length == 1) {
            return bookRepository.findAllByTitleContaining(keywords[0]);
        } else if (keywords.length == 2) {
            return bookRepository.findAllByTitleContainingAndTitleContaining(
                keywords[0], keywords[1]);
        } else if (keywords.length == 3) {
            return bookRepository.findAllByTitleContainingAndTitleContainingAndTitleContaining(
                keywords[0], keywords[1], keywords[2]);
        }
        return null;
    }

    @Measure(value = "3 keywords indexed in DB", warmup = 50)
    @GetMapping("keywords1/{keywordsString}")
    @Transactional(readOnly = true)
    public List<Book> getBookByTitleDB(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");
        if (keywords.length == 1) {
            List<Book> list1 = bookRepository.findByKeyword1(keywords[0]);
            List<Book> list2 = bookRepository.findByKeyword2(keywords[0]);
            List<Book> list3 = bookRepository.findByKeyword3(keywords[0]);
            list1.addAll(list2);
            list1.addAll(list3);
            return list1;
        } else if (keywords.length == 2) {
            List<Book> list1 = bookRepository.findByKeyword1AndKeyword2(
                    keywords[0], keywords[1]);
            List<Book> list2 = bookRepository.findByKeyword1AndKeyword2(
                    keywords[1], keywords[0]);
            List<Book> list3 = bookRepository.findByKeyword2AndKeyword3(
                    keywords[0], keywords[1]);
            List<Book> list4 = bookRepository.findByKeyword2AndKeyword3(
                    keywords[1], keywords[0]);
            List<Book> list5 = bookRepository.findByKeyword1AndKeyword3(
                    keywords[0], keywords[1]);
            List<Book> list6 = bookRepository.findByKeyword1AndKeyword3(
                    keywords[1], keywords[0]);
            list1.addAll(list2);
            list1.addAll(list3);
            list1.addAll(list4);
            list1.addAll(list5);
            list1.addAll(list6);
            return list1;
        } else if (keywords.length == 3) {
            List<Book> list1 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[0], keywords[1], keywords[2]);
            List<Book> list2 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[0], keywords[2], keywords[1]);
            List<Book> list3 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[1], keywords[0], keywords[2]);
            List<Book> list4 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[1], keywords[2], keywords[0]);
            List<Book> list5 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[2], keywords[1], keywords[0]);
            List<Book> list6 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[2], keywords[0], keywords[1]);
            list1.addAll(list2);
            list1.addAll(list3);
            list1.addAll(list4);
            list1.addAll(list5);
            list1.addAll(list6);
            return list1;
        }
        return null;
    }

    @Measure("test")
    @GetMapping("test")
    public String getTest() {
        return "just a test!";
    }

    @GetMapping("/random")
    public Book getBookRandom() {
        Random random = new Random();
        int index = random.nextInt(BOOKS_AMOUNT);
        List<Book> all = bookRepository.findAll();
        return all.get(index);
    }

    @Measure(value = "HashMap", warmup = 50)
    @GetMapping("keywords3/{keywordsString}")
    public Set<Book> getBookByTitleHashMap(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        Set<Book> bookSet = null;
        for (String keyword : keywords) {
            Set<Book> booksWithKeywordSet = new HashSet<>();
            for (int i = 0; i < Book.KEYWORDS_AMOUNT; i++) {
                Map<String, Set<Book>> map = Book.keywordMaps.get(i);
                if (map.containsKey(keyword)) {
                    booksWithKeywordSet.addAll(map.get(keyword));
                }
            }
            if (bookSet == null) {
                bookSet = booksWithKeywordSet;
            } else {
                bookSet.retainAll(booksWithKeywordSet);
            }
        }

        return bookSet;
    }

    @GetMapping
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("read-all")
    public void readAll() {
        List<Book> all = bookRepository.findAll();
        for (Book book: all) {
            Book.initKeywords(book);
        }
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        Book.initKeywords(book);
        return bookRepository.save(book);
    }

}
