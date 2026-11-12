package no.favourites.controller;

import no.favourites.repository.FavouriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FavouriteApiTest {

    private static final String FIRST = "e7ecd150-00af-42fb-9ca6-a215684703f3";
    private static final String SECOND = "48963eaa-e9cb-4736-a6a3-6a93ad43063e";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FavouriteRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private static RequestPostProcessor user(String subject) {
        return jwt().jwt(token -> token.subject(subject));
    }

    private void save(String subject, String analysisId, int expectedStatus) throws Exception {
        mvc.perform(post("/api/favourites").with(user(subject))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"analysisId\":\"" + analysisId + "\"}"))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void everyEndpointRequiresAToken() throws Exception {
        mvc.perform(get("/api/favourites")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/favourites").contentType(MediaType.APPLICATION_JSON)
                .content("{\"analysisId\":\"" + FIRST + "\"}")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/favourites/" + FIRST)).andExpect(status().isUnauthorized());

        assertThat(repository.count()).isZero();
    }

    @Test
    void savedFavouritesAreListedNewestFirstAndDeleteRemovesOnlyOne() throws Exception {
        save("alice", FIRST, 201);
        save("alice", SECOND, 201);

        mvc.perform(get("/api/favourites").with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].analysisId").value(SECOND))
                .andExpect(jsonPath("$[1].analysisId").value(FIRST))
                .andExpect(jsonPath("$[0].savedAt").exists());

        mvc.perform(delete("/api/favourites/" + FIRST).with(user("alice")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/favourites").with(user("alice")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].analysisId").value(SECOND));
    }

    @Test
    void savingTheSameAnalysisTwiceKeepsOneFavourite() throws Exception {
        save("alice", FIRST, 201);
        save("alice", FIRST, 200);

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void usersOnlySeeAndDeleteTheirOwnFavourites() throws Exception {
        save("alice", FIRST, 201);

        mvc.perform(get("/api/favourites").with(user("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(delete("/api/favourites/" + FIRST).with(user("bob")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/favourites").with(user("alice")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].analysisId").value(FIRST));
    }

    @Test
    void userIdComesFromTheTokenAndNotFromTheBody() throws Exception {
        mvc.perform(post("/api/favourites").with(user("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"analysisId\":\"" + FIRST + "\",\"userId\":\"bob\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/favourites").with(user("bob"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/favourites").with(user("alice"))).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invalidAnalysisIdIsRejectedAndNothingIsSaved() throws Exception {
        for (String body : new String[]{
                "{\"analysisId\":\"not-a-uuid\"}",
                "{\"analysisId\":\"1-1-1-1-1\"}",
                "{\"analysisId\":\"'; DROP TABLE favourites; --\"}",
                "{}",
                "not json"}) {
            mvc.perform(post("/api/favourites").with(user("alice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(delete("/api/favourites/not-a-uuid").with(user("alice")))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isZero();
    }
}
