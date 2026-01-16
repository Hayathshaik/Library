package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.InventoryEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryEventLogRepository extends JpaRepository<InventoryEventLog, Long> {

}

