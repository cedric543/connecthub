package use_case_tests;

import entity.CommonUser;
import entity.User;
import entity.UserFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import use_case.login.*;

import java.util.ArrayList;
import java.util.Collections;

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
        // make user
        String email = "email@gmail.com";
        String password = "securePassword";

        LoginInputData inputData = new LoginInputData(email, password);

        JSONObject userJson = new JSONObject();
        userJson.put("username", "Cedric");
        userJson.put("password", password);
        userJson.put("userId", "12345");
        userJson.put("birth_date", "2000-01-01");
        userJson.put("full_name", "Cedric Jizmejian");
        userJson.put("email", email);
        userJson.put("moderating", new JSONArray(Collections.emptyList()));
        userJson.put("posts", new JSONArray(Collections.emptyList()));

        when(mockPostDB.existsByEmail(email)).thenReturn(true);
        when(mockPostDB.getUserByEmail(email)).thenReturn(userJson);

        User mockUser = Mockito.mock(User.class);
        when(mockUser.getUsername()).thenReturn("Cedric");
        when(mockUser.getPassword()).thenReturn(password);
        when(mockUser.getEmail()).thenReturn(email);
        when(mockUser.getUserID()).thenReturn("12345");
        when(mockUser.getBirthDate()).thenReturn("2000-01-01");
        when(mockUser.getFullName()).thenReturn("Cedric Jizmejian");
        when(mockUser.getModerating()).thenReturn(new ArrayList<>());
        when(mockUser.getPosts()).thenReturn(new ArrayList<>());

        when(userFactory.create(
                "Cedric",
                password,
                "12345",
                "2000-01-01",
                "Cedric Jizmejian",
                email,
                new ArrayList<>(),
                new ArrayList<>()
        )).thenReturn(mockUser);

        ArgumentCaptor<LoginOutputData> captor = ArgumentCaptor.forClass(LoginOutputData.class);
        doNothing().when(mockPresenter).prepareSuccessView(captor.capture());

        interactor.LoginUser(inputData);

        // assert equals
        LoginOutputData capturedOutput = captor.getValue();
        assertEquals(email, capturedOutput.getUserEmail());
        assertTrue(capturedOutput.isLoginSuccessful());

        verify(mockPostDB).existsByEmail(email);
        verify(mockPostDB).getUserByEmail(email);
        verify(mockPresenter).prepareSuccessView(any(LoginOutputData.class));
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
        String email = "email@gmail.com";
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

        // assert equals
        IncorrectPasswordException exception = assertThrows(
                IncorrectPasswordException.class,
                () -> interactor.LoginUser(inputData)
        );

        assertEquals("Incorrect password for \"email@gmail.com\".", exception.getMessage());

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
}
