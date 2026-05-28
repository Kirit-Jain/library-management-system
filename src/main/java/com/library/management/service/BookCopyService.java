package com.library.management.service;

import com.library.management.dto.request.BookCopyCreateRequest;
import com.library.management.entity.*;
import com.library.management.enums.BookCondition;
import com.library.management.enums.BookCopyStatus;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.*;
import com.library.management.util.BarcodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final ShelfRepository shelfRepository;
    private final LibraryBranchRepository branchRepository;
    private final BarcodeGenerator barcodeGenerator;

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookCopy createCopy(BookCopyCreateRequest request) {
        Book book = bookRepository.findById(request.getBookId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Book", "id", request.getBookId()));
            
        String barcode = request.getBarcode();
        if (barcode == null || barcode.isBlank()) {
            barcode = barcodeGenerator.generate();
        }
    
        if (bookCopyRepository.existsByBarcode(barcode)) {
            throw new DuplicateResourceException(
                "Barcode already exists: " + barcode);
        }
    
        BookCopy copy = BookCopy.builder()
            .book(book)
            .barcode(barcode)
            .condition(request.getCondition() != null ? request.getCondition() : BookCondition.GOOD)
            .acquisitionDate(request.getAcquisitionDate())
            .price(request.getPrice())
            .status(BookCopyStatus.AVAILABLE)
            .build();
    
        if (request.getShelfId() != null) {
            Shelf shelf = shelfRepository.findById(request.getShelfId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Shelf", "id", request.getShelfId()));
            copy.setShelf(shelf);
        }
    
        if (request.getBranchId() != null) {
            LibraryBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Branch", "id", request.getBranchId()));
            copy.setBranch(branch);
        }
    
        BookCopy saved = bookCopyRepository.save(copy);
    
        book.setTotalCopies(book.getTotalCopies() + 1);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    
        log.info("Book copy added → barcode: {}, book: {}",
            saved.getBarcode(), book.getTitle());
    
        return saved;
    }

    public List<BookCopy> getCopiesByBook(Long bookId) {
        return bookCopyRepository.findAllByBookIdWithDetails(bookId);
    }

    public BookCopy getByBarcode(String barcode) {
        return bookCopyRepository.findByBarcode(barcode)
            .orElseThrow(() -> new ResourceNotFoundException(
                "BookCopy", "barcode", barcode));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public void deleteCopy(Long id) {
        BookCopy copy = bookCopyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BookCopy", "id", id));

        if (copy.getStatus() == BookCopyStatus.BORROWED) {
            throw new BadRequestException(
                "Cannot delete - book is currently borrowed");
        }

        // Update book counts
        Book book = copy.getBook();
        book.setTotalCopies(Math.max(0, book.getTotalCopies() - 1));
        if (copy.getStatus() == BookCopyStatus.AVAILABLE) {
            book.setAvailableCopies(Math.max(0, book.getAvailableCopies() - 1));
        }
        bookRepository.save(book);

        bookCopyRepository.delete(copy);
        log.info("Book copy deleted → id: {}", id);
    }

    public List<BookCopy> getAllCopies() {
        return bookCopyRepository.findAll();
    }
}