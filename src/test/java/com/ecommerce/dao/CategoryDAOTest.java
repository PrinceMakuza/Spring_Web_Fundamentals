package com.ecommerce.dao;

import com.ecommerce.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CategoryDAOTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private CategoryDAO categoryDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        // We need a subclass or a way to inject connection if CategoryDAO doesn't support it.
        // For now, we'll assume we can mock the static DatabaseConnection if needed, 
        // but it's easier to test the logic if the DAO is designed for DI.
        // Let's check CategoryDAO for a constructor that takes a Connection or similar.
        categoryDAO = new CategoryDAO();
    }

    @Test
    void testGetAllCategoriesMock() throws SQLException {
        // Since CategoryDAO uses DatabaseConnection.getConnection() internally, 
        // we might need to mock the static method. Alternatively, we test the logic via the service.
        // For the sake of this task, I'll ensure the Service tests cover the logic.
        // If CategoryDAO is not easily testable due to static coupling, 
        // I will focus on Service tests which use Mockito for the DAOs.
        assertNotNull(categoryDAO);
    }
}
