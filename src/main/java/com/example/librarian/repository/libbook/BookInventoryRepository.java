package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
    // Look up inventory by book id to validate/update quantities
    Optional<BookInventory> findByBookId(String bookId);

}
