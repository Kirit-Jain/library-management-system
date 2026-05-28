package com.library.management.repository;

import com.library.management.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("BookRepository Integration Tests")
class BookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        testBook = Book.builder()
            .isbn("9780061122415")
            .title("The Alchemist")
            .language("English")
            .totalCopies(3)
            .availableCopies(2)
            .build();

        bookRepository.save(testBook);
    }

    @Test
    @DisplayName("Should find book by ISBN")
    void findByIsbn_ShouldReturnBook_WhenIsbnExists() {
        Optional<Book> found = bookRepository.findByIsbn("9780061122415");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("The Alchemist");
    }

    @Test
    @DisplayName("Should return true when ISBN exists")
    void existsByIsbn_ShouldReturnTrue_WhenIsbnExists() {
        boolean exists = bookRepository.existsByIsbn("9780061122415");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when ISBN doesn't exist")
    void existsByIsbn_ShouldReturnFalse_WhenIsbnNotExist() {
        boolean exists = bookRepository.existsByIsbn("0000000000000");

        assertThat(exists).isFalse();
    }
}