package tn.esprit.spring.kaddem.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.kaddem.entities.Equipe;
import tn.esprit.spring.kaddem.entities.Niveau;
import tn.esprit.spring.kaddem.repositories.EquipeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipeServiceImplTest {

    @Mock
    private EquipeRepository equipeRepository;

    @InjectMocks
    private EquipeServiceImpl equipeService;

    private Equipe equipe1;
    private Equipe equipe2;

    @BeforeEach
    void setUp() {
        equipe1 = new Equipe(1, "Equipe A", Niveau.JUNIOR);
        equipe2 = new Equipe(2, "Equipe B", Niveau.SENIOR);
    }

    @Test
    void testRetrieveAllEquipes() {
        // Arrange
        when(equipeRepository.findAll()).thenReturn(Arrays.asList(equipe1, equipe2));

        // Act
        List<Equipe> equipes = equipeService.retrieveAllEquipes();

        // Assert
        assertEquals(2, equipes.size());
        verify(equipeRepository, times(1)).findAll();
    }

    @Test
    void testAddEquipe() {
        // Arrange
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe1);

        // Act
        Equipe savedEquipe = equipeService.addEquipe(equipe1);

        // Assert
        assertNotNull(savedEquipe);
        assertEquals("Equipe A", savedEquipe.getNomEquipe());
        verify(equipeRepository, times(1)).save(equipe1);
    }

    @Test
    void testDeleteEquipe() {
        // Arrange
        when(equipeRepository.findById(1)).thenReturn(Optional.of(equipe1));
        doNothing().when(equipeRepository).delete(equipe1);

        // Act
        equipeService.deleteEquipe(1);

        // Assert
        verify(equipeRepository, times(1)).delete(equipe1);
    }

    @Test
    void testRetrieveEquipe() {
        // Arrange
        when(equipeRepository.findById(1)).thenReturn(Optional.of(equipe1));

        // Act
        Equipe foundEquipe = equipeService.retrieveEquipe(1);

        // Assert
        assertNotNull(foundEquipe);
        assertEquals("Equipe A", foundEquipe.getNomEquipe());
        verify(equipeRepository, times(1)).findById(1);
    }

    @Test
    void testUpdateEquipe() {
        // Arrange
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe1);
        equipe1.setNomEquipe("Updated Equipe A");

        // Act
        Equipe updatedEquipe = equipeService.updateEquipe(equipe1);

        // Assert
        assertNotNull(updatedEquipe);
        assertEquals("Updated Equipe A", updatedEquipe.getNomEquipe());
        verify(equipeRepository, times(1)).save(equipe1);
    }

    @Test
    void testRetrieveEquipeNotFound() {
        // Arrange
        when(equipeRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            equipeService.retrieveEquipe(99);
        });
        verify(equipeRepository, times(1)).findById(99);
    }
}
