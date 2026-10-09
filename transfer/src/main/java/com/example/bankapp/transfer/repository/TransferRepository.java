package com.example.bankapp.transfer.repository;

import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends CrudRepository<Transfer, Long> {
    List<Transfer> findAllByStatusEquals(TransferStatus status);
}
