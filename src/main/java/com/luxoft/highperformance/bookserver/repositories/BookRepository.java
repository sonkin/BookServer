package com.luxoft.highperformance.bookserver.repositories;


import com.luxoft.highperformance.bookserver.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findAllByTitleContaining(String keyword);
    List<Book> findAllByTitleContainingAndTitleContaining(String keyword1, String keyword2);
    List<Book> findAllByTitleContainingAndTitleContainingAndTitleContaining(
        String keyword1, String keyword2, String keyword3);

}
