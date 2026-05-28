package com.library.management.service;

import com.library.management.dto.request.BookCreateRequest;
import com.library.management.dto.response.BookResponse;
import com.library.management.entity.*;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable(value = "books", key = "#id")
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return mapToResponse(book);
    }

    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "isbn", isbn));
        return mapToResponse(book);
    }

    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<BookResponse> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchBooks(query, pageable).map(this::mapToResponse);
    }

    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        return bookRepository.findByCategoryId(categoryId, pageable).map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookResponse createBook(BookCreateRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("Book with ISBN " + request.getIsbn() + " already exists");
        }
    
        Book book = Book.builder()
            .isbn(request.getIsbn())
            .title(request.getTitle())
            .description(request.getDescription())
            .publicationYear(request.getPublicationYear())
            .edition(request.getEdition())
            .language(request.getLanguage() != null ? request.getLanguage() : "English")
            .pageCount(request.getPageCount())
            .coverImageUrl(request.getCoverImageUrl())
            .totalCopies(0)
            .availableCopies(0)
            .build();
    
        // Set publisher (optional now)
        if (request.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new ResourceNotFoundException("Publisher", "id", request.getPublisherId()));
            book.setPublisher(publisher);
        } else {
            // Use or create "Unknown" publisher
            Publisher defaultPublisher = publisherRepository.findByName("Unknown")
                .orElseGet(() -> {
                    Publisher p = Publisher.builder()
                        .name("Unknown")
                        .build();
                    return publisherRepository.save(p);
                });
            book.setPublisher(defaultPublisher);
        }
    
        // Set authors (optional)
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.getAuthorIds()));
            book.setAuthors(authors);
        }
    
        // Set categories (optional)
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            book.setCategories(categories);
        }
    
        Book saved = bookRepository.save(book);
        log.info("Book created: isbn={}, title={}", saved.getIsbn(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        bookRepository.delete(book);
        log.info("Book deleted: id={}", id);
    }

    // ---- Mapping ----
    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
            .id(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .description(book.getDescription())
            .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : null)
            .publicationYear(book.getPublicationYear())
            .edition(book.getEdition())
            .language(book.getLanguage())
            .pageCount(book.getPageCount())
            .coverImageUrl(book.getCoverImageUrl())
            .totalCopies(book.getTotalCopies())
            .availableCopies(book.getAvailableCopies())
            .available(book.getAvailableCopies() != null && book.getAvailableCopies() > 0)
            .authors(book.getAuthors().stream()
                .map(Author::getFullName)
                .collect(Collectors.toSet()))
            .categories(book.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet()))
            .createdAt(book.getCreatedAt())
            .build();
    }
}