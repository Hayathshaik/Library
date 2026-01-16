package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
    // Look up inventory by book id to validate/update quantities
    Optional<BookInventory> findByBookId(String bookId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE BookInventory b
        SET b.quantity = b.quantity + :quantity
        WHERE b.bookId = :bookId
    """)
    int incrementBy(@Param("quantity") int quantity,
                    @Param("bookId") String bookId);
}
