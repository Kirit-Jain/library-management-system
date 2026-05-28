package com.library.management.service;

import com.library.management.dto.request.BookCreateRequest;
import com.library.management.dto.response.BookResponse;
import com.library.management.entity.Book;
import com.library.management.entity.Publisher;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.CategoryRepository;
import com.library.management.repository.PublisherRepository;
import com.library.management.service.BookService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private BookCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        Publisher publisher = Publisher.builder()
            .id(1L)
            .name("Penguin Books")
            .build();

        testBook = Book.builder()
            .id(1L)
            .isbn("9780061122415")
            .title("The Alchemist")
            .description("A novel by Paulo Coelho")
            .publisher(publisher)
            .publicationYear(1988)
            .language("English")
            .pageCount(208)
            .totalCopies(3)
            .availableCopies(2)
            .build();

        createRequest = new BookCreateRequest();
        createRequest.setIsbn("9780061122415");
        createRequest.setTitle("The Alchemist");
        createRequest.setDescription("Test description");
        createRequest.setPublicationYear(1988);
    }

    @Test
    @DisplayName("Should return book when valid ID is provided")
    void getBookById_ShouldReturnBook_WhenIdExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BookResponse result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("The Alchemist");
        assertThat(result.getIsbn()).isEqualTo("9780061122415");
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when book ID doesn't exist")
    void getBookById_ShouldThrowException_WhenIdNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book")
            .hasMessageContaining("999");

        verify(bookRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create book when ISBN is unique")
    void createBook_ShouldSucceed_WhenIsbnIsUnique() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookResponse result = bookService.createBook(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("9780061122415");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when ISBN already exists")
    void createBook_ShouldThrowException_WhenIsbnExists() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(createRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should return book by ISBN")
    void getBookByIsbn_ShouldReturnBook_WhenIsbnExists() {
        when(bookRepository.findByIsbn("9780061122415"))
            .thenReturn(Optional.of(testBook));

        BookResponse result = bookService.getBookByIsbn("9780061122415");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("The Alchemist");
    }

    @Test
    @DisplayName("Should delete book when it exists")
    void deleteBook_ShouldSucceed_WhenBookExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(testBook);
    }
}