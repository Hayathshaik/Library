package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
}

