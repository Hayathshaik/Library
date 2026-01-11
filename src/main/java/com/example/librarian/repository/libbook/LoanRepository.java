
package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Optimized exists check using standard Spring Data naming convention
    // This is safer than manual @Query for context startup validation
    boolean existsByBookIdAndState(String bookId, String state);

    @Query("SELECT l FROM Loan l WHERE l.userId = :userId AND l.bookId = :bookId AND l.state = 'Open'")
    Optional<Loan> findOpenLoanByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") String bookId);
}