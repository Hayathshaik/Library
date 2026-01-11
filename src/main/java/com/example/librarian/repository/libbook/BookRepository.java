package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    // FIX: Change 'findByBookid' to 'findByBookId' (Capital I)
    // Actually, since bookId is the @Id, you can just use the built-in findById()
    Optional<Book> findByBookId(String bookId);
}
