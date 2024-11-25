package use_case;

import entity.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import use_case.login.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginInteractorTest {

    private LoginDataAccessInterface mockPostDB;
    private LoginOutputBoundary mockPresenter;
    private LoginInteractor interactor;
    private UserFactory userFactory;

    @BeforeEach
    void setUp() {
        mockPostDB = Mockito.mock(LoginDataAccessInterface.class);
        mockPresenter = Mockito.mock(LoginOutputBoundary.class);
        userFactory = Mockito.mock(UserFactory.class);
        interactor = new LoginInteractor(mockPostDB, mockPresenter, userFactory);
    }


    @Test
    void loginSuccessTest() throws JSONException {

        String entryID = "123";
        String author = "Author1";
        String contentBody = "This is another test content.";
        String attachmentPath = "path/to/another/attachment";
        String fileType = "image/png";
        String title = "Another Post";
        String category = "Updates";
        LocalDateTime postedDate = LocalDateTime.now();
        LocalDateTime lastModified = LocalDateTime.now();
        int likes = 10;
        int dislikes = 1;

        JSONObject postJSON = new JSONObject();
        postJSON.put("post_id", entryID);
        postJSON.put("author", author);
        postJSON.put("content_body", contentBody);
        postJSON.put("attachment_path", attachmentPath);
        postJSON.put("file_type", fileType);
        postJSON.put("title", title);
        postJSON.put("category", category);
        postJSON.put("posted_date", postedDate.toString());
        postJSON.put("last_modified", lastModified.toString());
        postJSON.put("likes", likes);
        postJSON.put("dislikes", dislikes);
        postJSON.put("comments", new JSONArray());

        String mod1 = "mod1";
        String mod2 = "mod2";

        JSONArray modArray = new JSONArray();
        modArray.put(mod1);
        modArray.put(mod2);

        JSONArray postsArray = new JSONArray();
        postsArray.put(postJSON);

        String email = "email";
        String password = "password1";

        LoginInputData inputData = new LoginInputData(email, password);

        JSONObject userJson = new JSONObject();
        userJson.put("username", "Cedric");
        userJson.put("password", password);
        userJson.put("userId", "12345");
        userJson.put("birth_date", "2000-01-01");
        userJson.put("full_name", "Cedric Jizmejian");
        userJson.put("email", email);
        userJson.put("moderating", modArray);
        userJson.put("posts", postsArray);

        when(mockPostDB.existsByEmail(email)).thenReturn(true);
        when(mockPostDB.getUserByEmail(email)).thenReturn(userJson);

        List<String> expectedModerators = List.of(mod1, mod2);
        List<String> expectedPosts = List.of(entryID);

        User mockUser = Mockito.mock(User.class);
        when(mockUser.getUsername()).thenReturn("Cedric");
        when(mockUser.getPassword()).thenReturn(password);
        when(mockUser.getEmail()).thenReturn(email);
        when(mockUser.getUserID()).thenReturn("12345");
        when(mockUser.getBirthDate()).thenReturn("2000-01-01");
        when(mockUser.getFullName()).thenReturn("Cedric Jizmejian");
        when(mockUser.getModerating()).thenReturn(expectedModerators);
        when(mockUser.getPosts()).thenReturn(expectedPosts);

        when(userFactory.create(
                "Cedric",
                password,
                "12345",
                "2000-01-01",
                "Cedric Jizmejian",
                email,
                expectedModerators,
                expectedPosts
        )).thenReturn(mockUser);

        ArgumentCaptor<LoginOutputData> captor = ArgumentCaptor.forClass(LoginOutputData.class);
        doNothing().when(mockPresenter).prepareSuccessView(captor.capture());

        interactor.LoginUser(inputData);

        LoginOutputData capturedOutput = captor.getValue();
        assertEquals(email, capturedOutput.getUserEmail());
        assertTrue(capturedOutput.isLoginSuccessful());

        verify(mockPostDB).existsByEmail(email);
        verify(mockPostDB).getUserByEmail(email);
        verify(mockPresenter).prepareSuccessView(any(LoginOutputData.class));
    }



    @Test
    void incorrectPasswordExceptionTest() {
        String message = "Incorrect password";
        IncorrectPasswordException exception = new IncorrectPasswordException(message);
        assertEquals(message, exception.getMessage());
    }


    @Test
    void loginFailureNonExistentEmailTest() {
        // since no account was made there should be no email of this kind
        String email = "email@gmail.com";
        String password = "Password1";

        LoginInputData inputData = new LoginInputData(email, password);

        when(mockPostDB.existsByEmail(email)).thenReturn(false);

        doAnswer(invocation -> {
            String error = invocation.getArgument(0);
            assertEquals(email + ": Account does not exist.", error);
            return null;
        }).when(mockPresenter).prepareFailView(anyString());

        AccountDoesNotExistException exception = assertThrows(
                AccountDoesNotExistException.class,
                () -> interactor.LoginUser(inputData)
        );

        assertEquals(email + ": Account does not exist.", exception.getMessage());

        verify(mockPostDB).existsByEmail(email);
        verify(mockPresenter).prepareFailView(email + ": Account does not exist.");
    }


    @Test
    void loginFailureIncorrectPasswordTest() throws JSONException {
        // incorrect password leads to exception
        String email = "email";
        String correctPassword = "Password1";
        String incorrectPassword = "Password5";

        LoginInputData inputData = new LoginInputData(email, incorrectPassword);

        JSONObject userJson = new JSONObject();
        userJson.put("username", "Cedric");
        userJson.put("password", correctPassword);
        userJson.put("userId", "12345");
        userJson.put("birth_date", "2000-01-01");
        userJson.put("full_name", "Cedric Jizmejian");
        userJson.put("email", email);
        userJson.put("moderating", new JSONArray(Collections.emptyList()));
        userJson.put("posts", new JSONArray(Collections.emptyList()));



        when(mockPostDB.existsByEmail(email)).thenReturn(true);
        when(mockPostDB.getUserByEmail(email)).thenReturn(userJson);

        doAnswer(invocation -> {
            String error = invocation.getArgument(0);
            assertEquals("Incorrect password for \"" + email + "\".", error);
            return null;
        }).when(mockPresenter).prepareFailView(anyString());


        assertThrows(IncorrectPasswordException.class, ()
                -> interactor.LoginUser(inputData)
        );

        verify(mockPostDB).existsByEmail(email);
        verify(mockPostDB).getUserByEmail(email);
        verify(mockPresenter).prepareFailView("Incorrect password for \"" + email + "\".");
    }


    @Test
    void testUserFactoryConversion() throws JSONException {
        // make a user
        JSONObject userJson = new JSONObject();
        userJson.put("username", "Cedric");
        userJson.put("password", "Password1");
        userJson.put("userId", "12345");
        userJson.put("birth_date", "2000-01-01");
        userJson.put("full_name", "Cedric Jizmejian");
        userJson.put("email", "email@gmail.com");
        userJson.put("moderating", new JSONArray(Collections.emptyList()));
        userJson.put("posts", new JSONArray(Collections.emptyList()));

        User mockUser = new CommonUser("Cedric", "Password1", "12345", "2000-01-01", "Cedric Jizmejian", "test@example.com", new ArrayList<>(), new ArrayList<>());
        when(userFactory.create(
                "Cedric",
                "Password1",
                "12345",
                "2000-01-01",
                "Cedric Jizmejian",
                "email@gmail.com",
                new ArrayList<>(),
                new ArrayList<>()
        )).thenReturn(mockUser);

        User createdUser = userFactory.create(
                "Cedric",
                "Password1",
                "12345",
                "2000-01-01",
                "Cedric Jizmejian",
                "email@gmail.com",
                new ArrayList<>(),
                new ArrayList<>()
        );

        // assert equals
        assertEquals(mockUser.getUsername(), createdUser.getUsername());
        assertEquals(mockUser.getPassword(), createdUser.getPassword());
        assertEquals(mockUser.getUserID(), createdUser.getUserID());
    }

    @Test
    void loginFailureNullEmailTest() {
        LoginInputData inputData = new LoginInputData(null, "Password1");

        doAnswer(invocation -> {
            String error = invocation.getArgument(0);
            assertEquals("null: Account does not exist.", error);
            return null;
        }).when(mockPresenter).prepareFailView(anyString());

        AccountDoesNotExistException exception = assertThrows(
                AccountDoesNotExistException.class,
                () -> interactor.LoginUser(inputData)
        );

        assertEquals("null: Account does not exist.", exception.getMessage());
        verify(mockPresenter).prepareFailView("null: Account does not exist.");
    }

}
