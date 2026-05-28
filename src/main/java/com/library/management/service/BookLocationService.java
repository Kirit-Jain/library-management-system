package com.library.management.service;

import com.library.management.dto.response.BookLocationResponse;
import com.library.management.entity.BookCopy;
import com.library.management.entity.Borrowing;
import com.library.management.enums.BookCopyStatus;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BookCopyRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookLocationService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRepository borrowingRepository;

    public BookLocationResponse getBookLocation(Long bookId) {
        var book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        List<BookCopy> copies = bookCopyRepository
            .findAllByBookIdWithDetails(bookId);

        List<BookLocationResponse.CopyDetail> copyDetails = copies.stream()
            .map(copy -> buildCopyDetail(copy))
            .toList();

        long available = copies.stream()
            .filter(c -> c.getStatus() == BookCopyStatus.AVAILABLE)
            .count();

        return BookLocationResponse.builder()
            .bookId(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .totalCopies(copies.size())
            .availableCopies((int) available)
            .copies(copyDetails)
            .build();
    }

    private BookLocationResponse.CopyDetail buildCopyDetail(BookCopy copy) {
        var builder = BookLocationResponse.CopyDetail.builder()
            .copyId(copy.getId())
            .barcode(copy.getBarcode())
            .status(copy.getStatus())
            .condition(copy.getCondition());

        // Build location string
        if (copy.getBranch() != null) {
            builder.branchName(copy.getBranch().getName());
            builder.branchAddress(copy.getBranch().getAddress());
        }

        if (copy.getShelf() != null) {
            var shelf = copy.getShelf();
            builder.shelfCode(shelf.getShelfCode());

            if (shelf.getSection() != null) {
                var section = shelf.getSection();
                builder.sectionName(section.getName());
                builder.sectionCode(section.getCode());

                if (section.getFloor() != null) {
                    var floor = section.getFloor();
                    builder.floorName(floor.getName());
                    builder.floorNumber(floor.getFloorNumber());
                }
            }

            // Build human readable location description
            builder.locationDescription(buildLocationString(copy));
        }

        // If borrowed - when is it expected back
        if (copy.getStatus() == BookCopyStatus.BORROWED) {
            borrowingRepository.findActiveByBarcode(copy.getBarcode())
                .ifPresent(b -> builder.expectedReturnDate(b.getDueDate()));
        }

        return builder.build();
    }

    private String buildLocationString(BookCopy copy) {
        StringBuilder sb = new StringBuilder();

        if (copy.getBranch() != null) {
            sb.append(copy.getBranch().getName());
        }
        if (copy.getShelf() != null) {
            var shelf = copy.getShelf();
            if (shelf.getSection() != null) {
                var section = shelf.getSection();
                if (section.getFloor() != null) {
                    sb.append(" → Floor ").append(section.getFloor().getFloorNumber());
                }
                sb.append(" → ").append(section.getName());
                sb.append(" (").append(section.getCode()).append(")");
            }
            sb.append(" → Shelf ").append(shelf.getShelfCode());
        }

        return sb.toString();
    }
}