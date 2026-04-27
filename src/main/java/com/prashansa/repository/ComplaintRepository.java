package com.prashansa.repository;

import com.prashansa.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, String> {
}
