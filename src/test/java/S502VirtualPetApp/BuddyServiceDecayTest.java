package S502VirtualPetApp;


import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualBuddy;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import S502VirtualPetApp.service.BuddyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BuddyServiceDecayTest {

    @Mock
    private VirtualBuddyRepository buddyRepository;

    @InjectMocks
    private BuddyService buddyService;

    private VirtualBuddy buddy;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        User dummyUser = new User();
        dummyUser.setId("user123");

        buddy = new VirtualBuddy("TestBuddy", "default", dummyUser);
        buddy.setHappiness(80);
        buddy.setCreatedAt(LocalDateTime.now().minusDays(10));
        buddy.setUpdatedAt(LocalDateTime.now().minusDays(10));
    }

    @Test
    public void shouldDecayHappinessAfter2DaysOfInactivity() throws Exception {
        buddy.setLastMeditation(LocalDateTime.now().minusDays(3));
        buddy.setLastHug(null);
        buddy.setLastHappinessCheck(LocalDateTime.now().minusDays(3));
        buddy.setHappiness(80);

        // Llamamos el método privado usando reflexión
        var method = BuddyService.class.getDeclaredMethod("decayHappinessIfInactive", VirtualBuddy.class);
        method.setAccessible(true);
        method.invoke(buddyService, buddy);

        verify(buddyRepository, times(1)).save(any(VirtualBuddy.class));
        assertEquals(65, buddy.getHappiness(), "Debería decaer 10 puntos (2 días * 5)");
    }

    @Test
    public void shouldNotGoBelowZeroHappiness() throws Exception {
        buddy.setHappiness(5);
        buddy.setLastHug(LocalDateTime.now().minusDays(10));
        buddy.setLastHappinessCheck(LocalDateTime.now().minusDays(10));

        var method = BuddyService.class.getDeclaredMethod("decayHappinessIfInactive", VirtualBuddy.class);
        method.setAccessible(true);
        method.invoke(buddyService, buddy);

        assertEquals(0, buddy.getHappiness(), "La felicidad no debería ser menor a 0");
    }

    @Test
    public void shouldUseLatestInteractionAsReference() throws Exception {
        buddy.setLastHug(LocalDateTime.now().minusDays(1)); // más reciente que meditation
        buddy.setLastMeditation(LocalDateTime.now().minusDays(3));
        buddy.setLastHappinessCheck(LocalDateTime.now().minusDays(5));
        buddy.setHappiness(90);

        var method = BuddyService.class.getDeclaredMethod("decayHappinessIfInactive", VirtualBuddy.class);
        method.setAccessible(true);
        method.invoke(buddyService, buddy);

        // Solo 1 día desde el último hug => decay = 5
        assertEquals(85, buddy.getHappiness(), "La felicidad debería decaer 5 puntos");
    }
}
